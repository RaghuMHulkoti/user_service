package com.learning.user_service.infrastructure.messaging;

import com.learning.user_service.domain.port.EventPublisher;
import com.learning.user_service.domain.port.NonRetryablePublishException;
import com.learning.user_service.infrastructure.config.AppProperties;
import com.learning.user_service.infrastructure.persistence.entity.OutboxStatus;
import com.learning.user_service.infrastructure.persistence.entity.UserOutboxEntity;
import com.learning.user_service.infrastructure.persistence.repository.UserOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Polls {@code user_outbox} and publishes PENDING rows (TDD §20.6).
 * <ul>
 *   <li>Uses {@code FOR UPDATE SKIP LOCKED}, so several replicas can poll safely.</li>
 *   <li>A failed publish leaves the row PENDING for the next poll (TDD §13.8), so a broker
 *       outage never loses events. The batch stops there to keep per-user order.</li>
 *   <li>Only an event the broker will never accept is marked FAILED and skipped.</li>
 * </ul>
 */
@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final UserOutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final int batchSize;
    private final Clock clock;

    public OutboxPoller(UserOutboxRepository outboxRepository, EventPublisher eventPublisher,
                        TransactionTemplate transactionTemplate, AppProperties properties, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.transactionTemplate = transactionTemplate;
        this.batchSize = properties.outbox().batchSize();
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval}")
    public void poll() {
        try {
            Integer published = transactionTemplate.execute(status -> publishBatch());
            if (published != null && published > 0) {
                log.debug("Outbox published {} event(s)", published);
            }
        } catch (RuntimeException ex) {
            log.warn("Outbox poll failed: {}", ex.getMessage());
        }
    }

    int publishBatch() {
        List<UserOutboxEntity> batch = outboxRepository.lockPendingBatch(batchSize);
        int published = 0;
        for (UserOutboxEntity row : batch) {
            try {
                eventPublisher.publish(row.getAggregateId().toString(), row.getEventType(), row.getPayload());
                row.setStatus(OutboxStatus.SENT);
                row.setSentAt(Instant.now(clock));
                row.setLastError(null);
                published++;
            } catch (NonRetryablePublishException ex) {
                row.setAttempts(row.getAttempts() + 1);
                row.setLastError(truncate(ex.getMessage()));
                row.setStatus(OutboxStatus.FAILED);
                log.error("Outbox event {} ({}) moved to FAILED: {}", row.getId(), row.getEventType(),
                        ex.getMessage());
            } catch (RuntimeException ex) {
                row.setAttempts(row.getAttempts() + 1);
                row.setLastError(truncate(ex.getMessage()));
                log.warn("Outbox event {} publish failed (attempt {}), will retry: {}",
                        row.getId(), row.getAttempts(), ex.getMessage());
                break;
            }
        }
        return published;
    }

    private static String truncate(String message) {
        if (message == null) {
            return "unknown error";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
