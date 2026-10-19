package com.learning.user_service.core.service.auth.login;

import com.learning.user_service.config.AppProperties;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.EmailNormalizer;
import com.learning.user_service.core.support.SessionSupport;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.PasswordCredentialGateway;
import com.learning.user_service.gateway.RateLimiter;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Login (TDD §12.2, §15.1): rate-limited (5/min/IP, 10/hour/account), locks the account after
 * repeated wrong passwords, issues an access + refresh token pair.
 */
@Component
public class LoginService implements Service<LoginServiceRequest, LoginServiceResponse> {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UserGateway userGateway;
    private final PasswordCredentialGateway credentialGateway;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;
    private final SessionSupport sessionSupport;
    private final AppProperties.Security security;
    private final AppProperties.RateLimit limits;
    private final Clock clock;

    public LoginService(UserGateway userGateway, PasswordCredentialGateway credentialGateway,
                        PasswordEncoder passwordEncoder, RateLimiter rateLimiter, SessionSupport sessionSupport,
                        AppProperties properties, Clock clock) {
        this.userGateway = userGateway;
        this.credentialGateway = credentialGateway;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.sessionSupport = sessionSupport;
        this.security = properties.security();
        this.limits = properties.rateLimit();
        this.clock = clock;
    }

    /** Failed-attempt counters must be saved even though the call ends in an error. */
    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public LoginServiceResponse execute(LoginServiceRequest request) {
        String clientIp = request.getClientIp();
        String email = EmailNormalizer.normalize(request.getEmail());
        if (!rateLimiter.tryAcquire("login:ip:" + clientIp, limits.loginPerIpPerMinute(), Duration.ofMinutes(1))
                || !rateLimiter.tryAcquire("login:account:" + email, limits.loginPerAccountPerHour(),
                Duration.ofHours(1))) {
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many login attempts; try again later");
        }

        Optional<UserEntity> found = userGateway.findActiveByEmail(email);
        Optional<PasswordCredentialEntity> credential = found.flatMap(u -> credentialGateway.findById(u.getId()));
        if (found.isEmpty() || credential.isEmpty()) {
            passwordEncoder.matches(request.getPassword(), sessionSupport.dummyHash());
            throw SessionSupport.invalidCredentials();
        }
        UserEntity user = found.get();
        PasswordCredentialEntity cred = credential.get();
        Instant now = clock.instant();

        if (cred.isLocked(now)) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "Account is temporarily locked; try again later");
        }
        if (!passwordEncoder.matches(request.getPassword(), cred.getPasswordHash())) {
            registerFailure(cred, now);
            throw SessionSupport.invalidCredentials();
        }
        if (!user.getStatus().canLogIn()) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "Account is not active");
        }

        cred.setFailedAttempts(0);
        cred.setLockedUntil(null);
        userGateway.recordLogin(user.getId(), now);
        log.info("User {} logged in", user.getId());
        return LoginServiceResponse.builder().tokenPair(sessionSupport.issueTokens(user)).build();
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
}
