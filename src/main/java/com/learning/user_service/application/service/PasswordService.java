package com.learning.user_service.application.service;

import com.learning.user_service.application.command.ChangePasswordCommand;
import com.learning.user_service.application.command.PasswordResetCommand;
import com.learning.user_service.application.command.PasswordResetRequestCommand;
import com.learning.user_service.application.support.DevTokenLogger;
import com.learning.user_service.application.support.EmailNormalizer;
import com.learning.user_service.application.support.SecureTokens;
import com.learning.user_service.domain.event.UserEventType;
import com.learning.user_service.domain.policy.PasswordPolicy;
import com.learning.user_service.domain.port.DomainEventRecorder;
import com.learning.user_service.domain.port.RateLimiter;
import com.learning.user_service.domain.port.RefreshTokenStore;
import com.learning.user_service.infrastructure.config.AppProperties;
import com.learning.user_service.infrastructure.persistence.entity.PasswordCredentialEntity;
import com.learning.user_service.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.learning.user_service.infrastructure.persistence.entity.UserEntity;
import com.learning.user_service.infrastructure.persistence.repository.PasswordCredentialRepository;
import com.learning.user_service.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.learning.user_service.infrastructure.persistence.repository.UserRepository;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Password reset by email link, and password change for a signed-in user. */
@Service
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStore refreshTokenStore;
    private final DomainEventRecorder events;
    private final RateLimiter rateLimiter;
    private final DevTokenLogger devTokenLogger;
    private final UserAccess userAccess;
    private final AppProperties properties;
    private final Clock clock;

    public PasswordService(UserRepository userRepository, PasswordCredentialRepository credentialRepository,
                           PasswordResetTokenRepository resetTokenRepository, PasswordEncoder passwordEncoder,
                           RefreshTokenStore refreshTokenStore, DomainEventRecorder events, RateLimiter rateLimiter,
                           DevTokenLogger devTokenLogger, UserAccess userAccess, AppProperties properties,
                           Clock clock) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenStore = refreshTokenStore;
        this.events = events;
        this.rateLimiter = rateLimiter;
        this.devTokenLogger = devTokenLogger;
        this.userAccess = userAccess;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Always succeeds from the caller's view, whether or not the email exists,
     * so the endpoint cannot be used to discover accounts.
     */
    @Transactional
    public void requestReset(PasswordResetRequestCommand cmd) {
        String email = EmailNormalizer.normalize(cmd.email());
        Optional<UserEntity> found = userRepository.findActiveByEmail(email).filter(u -> u.getStatus().canLogIn());
        if (found.isEmpty()) {
            return;
        }
        if (!rateLimiter.tryAcquire("password-reset:" + email,
                properties.rateLimit().passwordResetPerAccountPerHour(), Duration.ofHours(1))) {
            return; // silently drop: same response as success
        }
        UserEntity user = found.get();
        Instant now = clock.instant();
        resetTokenRepository.consumeAllOpen(user.getId(), now);

        String rawToken = SecureTokens.newToken();
        Instant expiresAt = now.plus(properties.tokens().passwordResetTtl());
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUserId(user.getId());
        token.setTokenHash(SecureTokens.sha256Hex(rawToken));
        token.setExpiresAt(expiresAt);
        resetTokenRepository.save(token);

        events.record(user.getId(), UserEventType.PASSWORD_RESET_REQUESTED, Map.of(
                "userId", user.getId().toString(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "locale", user.getLocale(),
                "resetToken", rawToken,
                "expiresAt", expiresAt.toString()));
        devTokenLogger.log("Password reset", user.getId(), rawToken);
    }

    @Transactional
    public void reset(PasswordResetCommand cmd) {
        requireStrong(cmd.newPassword(), "newPassword");
        Instant now = clock.instant();
        PasswordResetTokenEntity token = resetTokenRepository.findByTokenHash(SecureTokens.sha256Hex(cmd.token()))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Reset link is invalid or has expired"));
        token.setConsumedAt(now);

        UserEntity user = userAccess.loadUsable(token.getUserId());
        PasswordCredentialEntity cred = credentialRepository.findById(user.getId())
                .orElseGet(() -> new PasswordCredentialEntity(user.getId(), ""));
        cred.setPasswordHash(passwordEncoder.encode(cmd.newPassword()));
        cred.setFailedAttempts(0);
        cred.setLockedUntil(null);
        credentialRepository.save(cred);

        // Clicking the emailed link proves the user owns the address.
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(now);
        }
        passwordChanged(user.getId(), "RESET");
    }

    @Transactional
    public void change(UUID userId, ChangePasswordCommand cmd) {
        UserEntity user = userAccess.loadUsable(userId);
        PasswordCredentialEntity cred = credentialRepository.findById(user.getId())
                .orElseThrow(() -> ApiException.notFound("Credentials"));
        if (!passwordEncoder.matches(cmd.currentPassword(), cred.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect");
        }
        requireStrong(cmd.newPassword(), "newPassword");
        cred.setPasswordHash(passwordEncoder.encode(cmd.newPassword()));
        passwordChanged(user.getId(), "CHANGE");
    }

    /** Signs the user out everywhere once the new password is committed. */
    private void passwordChanged(UUID userId, String reason) {
        events.record(userId, UserEventType.PASSWORD_CHANGED, Map.of("userId", userId.toString(), "reason", reason));
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                refreshTokenStore.revokeAllForUser(userId);
            }
        });
    }

    private static void requireStrong(String password, String field) {
        List<String> problems = PasswordPolicy.violations(password);
        if (!problems.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Request validation failed",
                    Map.of("fields", Map.of(field, String.join("; ", problems))));
        }
    }
}
