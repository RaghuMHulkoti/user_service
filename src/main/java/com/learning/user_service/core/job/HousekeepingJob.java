package com.learning.user_service.core.job;

import com.learning.user_service.config.AppProperties;
import com.learning.user_service.gateway.EmailVerificationGateway;
import com.learning.user_service.gateway.IdempotencyGateway;
import com.learning.user_service.gateway.OutboxGateway;
import com.learning.user_service.gateway.PasswordResetTokenGateway;
import com.learning.user_service.gateway.RefreshTokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Deletes old rows: sent outbox events, expired idempotency keys, used/expired one-time tokens. */
@Component
public class HousekeepingJob {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingJob.class);
    private static final Duration SENT_EVENT_RETENTION = Duration.ofDays(7);
    private static final Duration TOKEN_RETENTION = Duration.ofDays(7);

    private final OutboxGateway outboxGateway;
    private final IdempotencyGateway idempotencyGateway;
    private final EmailVerificationGateway emailVerificationGateway;
    private final PasswordResetTokenGateway passwordResetTokenGateway;
    private final RefreshTokenStore refreshTokenStore;
    private final Duration idempotencyTtl;
    private final Clock clock;

    public HousekeepingJob(OutboxGateway outboxGateway, IdempotencyGateway idempotencyGateway,
                           EmailVerificationGateway emailVerificationGateway,
                           PasswordResetTokenGateway passwordResetTokenGateway, RefreshTokenStore refreshTokenStore,
                           AppProperties properties, Clock clock) {
        this.outboxGateway = outboxGateway;
        this.idempotencyGateway = idempotencyGateway;
        this.emailVerificationGateway = emailVerificationGateway;
        this.passwordResetTokenGateway = passwordResetTokenGateway;
        this.refreshTokenStore = refreshTokenStore;
        this.idempotencyTtl = properties.idempotency().ttl();
        this.clock = clock;
    }

    @Scheduled(cron = "0 15 * * * *")
    public void cleanUp() {
        Instant now = Instant.now(clock);
        int outbox = outboxGateway.deleteSentBefore(now.minus(SENT_EVENT_RETENTION));
        int keys = idempotencyGateway.deleteCreatedBefore(now.minus(idempotencyTtl));
        Instant tokenCutoff = now.minus(TOKEN_RETENTION);
        int verifications = emailVerificationGateway.deleteExpiredBefore(tokenCutoff);
        int resets = passwordResetTokenGateway.deleteExpiredBefore(tokenCutoff);
        int refreshTokens = refreshTokenStore.deleteExpired(now);
        log.info("Housekeeping removed outbox={} idempotencyKeys={} emailVerifications={} passwordResets={} refreshTokens={}",
                outbox, keys, verifications, resets, refreshTokens);
    }
}
