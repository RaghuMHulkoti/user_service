package com.learning.user_service.infrastructure.messaging;

import com.learning.user_service.infrastructure.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Deletes old rows: sent outbox events, expired idempotency keys, used/expired one-time tokens. */
@Component
public class HousekeepingJob {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingJob.class);
    private static final Duration SENT_EVENT_RETENTION = Duration.ofDays(7);
    private static final Duration TOKEN_RETENTION = Duration.ofDays(7);

    private final JdbcTemplate jdbcTemplate;
    private final Duration idempotencyTtl;
    private final Clock clock;

    public HousekeepingJob(JdbcTemplate jdbcTemplate, AppProperties properties, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.idempotencyTtl = properties.idempotency().ttl();
        this.clock = clock;
    }

    @Scheduled(cron = "0 15 * * * *")
    public void cleanUp() {
        Instant now = Instant.now(clock);
        int outbox = jdbcTemplate.update("delete from user_outbox where status = 'SENT' and sent_at < ?",
                Timestamp.from(now.minus(SENT_EVENT_RETENTION)));
        int keys = jdbcTemplate.update("delete from idempotency_keys where created_at < ?",
                Timestamp.from(now.minus(idempotencyTtl)));
        Timestamp tokenCutoff = Timestamp.from(now.minus(TOKEN_RETENTION));
        int verifications = jdbcTemplate.update("delete from email_verifications where expires_at < ?", tokenCutoff);
        int resets = jdbcTemplate.update("delete from password_reset_tokens where expires_at < ?", tokenCutoff);
        log.info("Housekeeping removed outbox={} idempotencyKeys={} emailVerifications={} passwordResets={}",
                outbox, keys, verifications, resets);
    }
}
