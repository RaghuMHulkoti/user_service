package com.learning.user_service.application.service;

import com.learning.user_service.application.command.LoginCommand;
import com.learning.user_service.application.command.LogoutCommand;
import com.learning.user_service.application.command.RefreshTokenCommand;
import com.learning.user_service.application.query.TokenPair;
import com.learning.user_service.application.support.EmailNormalizer;
import com.learning.user_service.application.support.SecureTokens;
import com.learning.user_service.domain.port.AccessTokenFactory;
import com.learning.user_service.domain.port.RateLimiter;
import com.learning.user_service.domain.port.RefreshTokenStore;
import com.learning.user_service.infrastructure.config.AppProperties;
import com.learning.user_service.infrastructure.persistence.entity.PasswordCredentialEntity;
import com.learning.user_service.infrastructure.persistence.entity.UserEntity;
import com.learning.user_service.infrastructure.persistence.repository.PasswordCredentialRepository;
import com.learning.user_service.infrastructure.persistence.repository.UserRepository;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Login, refresh and logout (TDD §12.2, §15.1).
 * <ul>
 *   <li>Access token: RS256 JWT, 15 min.</li>
 *   <li>Refresh token: opaque {@code <jti>.<secret>}, 30 days, stored hashed in Redis,
 *       rotated on every refresh, revoked on logout.</li>
 *   <li>Login is rate-limited (5/min/IP, 10/hour/account) and locks the account after
 *       repeated wrong passwords.</li>
 * </ul>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenFactory accessTokenFactory;
    private final RefreshTokenStore refreshTokenStore;
    private final RateLimiter rateLimiter;
    private final AppProperties.Security security;
    private final AppProperties.RateLimit limits;
    private final Clock clock;
    /** Compared against when the email is unknown, so response time does not reveal which emails exist. */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordCredentialRepository credentialRepository,
                       PasswordEncoder passwordEncoder, AccessTokenFactory accessTokenFactory,
                       RefreshTokenStore refreshTokenStore, RateLimiter rateLimiter, AppProperties properties,
                       Clock clock) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenFactory = accessTokenFactory;
        this.refreshTokenStore = refreshTokenStore;
        this.rateLimiter = rateLimiter;
        this.security = properties.security();
        this.limits = properties.rateLimit();
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing-" + UUID.randomUUID());
    }

    /** Failed-attempt counters must be saved even though the call ends in an error. */
    @Transactional(noRollbackFor = ApiException.class)
    public TokenPair login(LoginCommand cmd, String clientIp) {
        String email = EmailNormalizer.normalize(cmd.email());
        if (!rateLimiter.tryAcquire("login:ip:" + clientIp, limits.loginPerIpPerMinute(), Duration.ofMinutes(1))
                || !rateLimiter.tryAcquire("login:account:" + email, limits.loginPerAccountPerHour(),
                Duration.ofHours(1))) {
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many login attempts; try again later");
        }

        Optional<UserEntity> found = userRepository.findActiveByEmail(email);
        Optional<PasswordCredentialEntity> credential = found.flatMap(u -> credentialRepository.findById(u.getId()));
        if (found.isEmpty() || credential.isEmpty()) {
            passwordEncoder.matches(cmd.password(), dummyHash);
            throw invalidCredentials();
        }
        UserEntity user = found.get();
        PasswordCredentialEntity cred = credential.get();
        Instant now = clock.instant();

        if (cred.isLocked(now)) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "Account is temporarily locked; try again later");
        }
        if (!passwordEncoder.matches(cmd.password(), cred.getPasswordHash())) {
            registerFailure(cred, now);
            throw invalidCredentials();
        }
        if (!user.getStatus().canLogIn()) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "Account is not active");
        }

        cred.setFailedAttempts(0);
        cred.setLockedUntil(null);
        userRepository.recordLogin(user.getId(), now);
        log.info("User {} logged in", user.getId());
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public TokenPair refresh(RefreshTokenCommand cmd) {
        ParsedRefreshToken parsed = ParsedRefreshToken.parse(cmd.refreshToken())
                .orElseThrow(AuthService::invalidRefreshToken);
        // Rotation: consume() deletes the old token atomically, so it works exactly once.
        RefreshTokenStore.StoredRefreshToken stored = refreshTokenStore.consume(parsed.jti())
                .filter(t -> SecureTokens.constantTimeEquals(t.secretHash(), SecureTokens.sha256Hex(parsed.secret())))
                .orElseThrow(AuthService::invalidRefreshToken);

        UserEntity user = userRepository.findById(stored.userId())
                .filter(u -> u.getStatus().canLogIn())
                .orElseThrow(AuthService::invalidRefreshToken);
        return issueTokens(user);
    }

    public void logout(UUID userId, LogoutCommand cmd) {
        if (cmd.everywhere()) {
            refreshTokenStore.revokeAllForUser(userId);
            return;
        }
        if (cmd.refreshToken() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "refreshToken is required unless allSessions is true");
        }
        ParsedRefreshToken.parse(cmd.refreshToken())
                .flatMap(parsed -> refreshTokenStore.find(parsed.jti()))
                .filter(stored -> stored.userId().equals(userId))
                .ifPresent(stored -> refreshTokenStore.revoke(stored.jti()));
    }

    private TokenPair issueTokens(UserEntity user) {
        AccessTokenFactory.IssuedAccessToken access = accessTokenFactory.issue(user.getId(), user.roleCodes());
        String jti = UUID.randomUUID().toString();
        String secret = SecureTokens.newToken();
        refreshTokenStore.save(jti, user.getId(), SecureTokens.sha256Hex(secret), security.refreshTokenTtl());
        return new TokenPair(access.value(), jti + "." + secret, TOKEN_TYPE,
                access.ttl().toSeconds(), security.refreshTokenTtl().toSeconds());
    }

    private void registerFailure(PasswordCredentialEntity cred, Instant now) {
        int attempts = cred.getFailedAttempts() + 1;
        if (attempts >= security.maxFailedLogins()) {
            cred.setLockedUntil(now.plus(security.lockoutDuration()));
            cred.setFailedAttempts(0);
            log.warn("User {} locked until {} after {} failed logins", cred.getUserId(), cred.getLockedUntil(),
                    attempts);
        } else {
            cred.setFailedAttempts(attempts);
        }
    }

    private static ApiException invalidCredentials() {
        return new ApiException(ErrorCode.INVALID_CREDENTIALS, "Email or password is incorrect");
    }

    private static ApiException invalidRefreshToken() {
        return new ApiException(ErrorCode.INVALID_TOKEN, "Refresh token is invalid, expired or revoked");
    }

    record ParsedRefreshToken(String jti, String secret) {

        static Optional<ParsedRefreshToken> parse(String raw) {
            if (raw == null) {
                return Optional.empty();
            }
            int dot = raw.indexOf('.');
            if (dot <= 0 || dot == raw.length() - 1) {
                return Optional.empty();
            }
            String jti = raw.substring(0, dot);
            try {
                UUID.fromString(jti);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            return Optional.of(new ParsedRefreshToken(jti, raw.substring(dot + 1)));
        }
    }
}
