package com.learning.user_service.core.support;

import com.learning.user_service.config.AppProperties;
import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.EmailVerificationGateway;
import com.learning.user_service.gateway.oracle.credential.entity.EmailVerificationEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/** Creates an email-verification token and records the event that asks the notification service to send it. */
@Component
public class VerificationTokenIssuer {

    private final EmailVerificationGateway verificationGateway;
    private final DomainEventRecorder events;
    private final DevTokenLogger devTokenLogger;
    private final Duration verificationTtl;
    private final Clock clock;

    public VerificationTokenIssuer(EmailVerificationGateway verificationGateway, DomainEventRecorder events,
                                   DevTokenLogger devTokenLogger, AppProperties properties, Clock clock) {
        this.verificationGateway = verificationGateway;
        this.events = events;
        this.devTokenLogger = devTokenLogger;
        this.verificationTtl = properties.tokens().emailVerificationTtl();
        this.clock = clock;
    }

    public void issue(UserEntity user) {
        String rawToken = SecureTokens.newToken();
        Instant expiresAt = clock.instant().plus(verificationTtl);
        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setUserId(user.getId());
        verification.setTokenHash(SecureTokens.sha256Hex(rawToken));
        verification.setExpiresAt(expiresAt);
        verificationGateway.save(verification);

        // The notification service needs the raw token to build the link. Email failures are
        // outboxed and never fail registration (TDD §8.2).
        events.record(user.getId(), UserEventType.EMAIL_VERIFICATION_REQUESTED, Map.of(
                "userId", user.getId().toString(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "locale", user.getLocale(),
                "verificationToken", rawToken,
                "expiresAt", expiresAt.toString()));
        devTokenLogger.log("Email verification", user.getId(), rawToken);
    }
}
