package com.learning.user_service.core.job;

import com.learning.user_service.gateway.EventPublisher;
import com.learning.user_service.exception.NonRetryablePublishException;
import com.learning.user_service.gateway.oracle.outbox.entity.OutboxStatus;
import com.learning.user_service.gateway.oracle.outbox.entity.UserOutboxEntity;
import com.learning.user_service.gateway.OutboxGateway;
import com.learning.user_service.support.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");

    @Mock OutboxGateway repository;
    @Mock EventPublisher publisher;
    @Mock com.learning.user_service.core.support.UserMailNotifier mailNotifier;
    @Mock TransactionTemplate transactionTemplate;

    private OutboxPoller poller;

    @BeforeEach
    void setUp() {
        poller = new OutboxPoller(repository, publisher, mailNotifier, transactionTemplate, TestProperties.defaults(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void publishedRowsBecomeSent() {
        UserOutboxEntity first = row("USER_REGISTERED");
        UserOutboxEntity second = row("USER_UPDATED");
        when(repository.lockPendingBatch(100)).thenReturn(List.of(first, second));

        assertThat(poller.publishBatch()).isEqualTo(2);

        assertThat(first.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(first.getSentAt()).isEqualTo(NOW);
        assertThat(second.getStatus()).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    void brokerOutageKeepsRowPendingAndStopsBatch() {
        UserOutboxEntity first = row("USER_REGISTERED");
        UserOutboxEntity second = row("USER_UPDATED");
        when(repository.lockPendingBatch(100)).thenReturn(List.of(first, second));
        doThrow(new IllegalStateException("broker down"))
                .when(publisher).publish(anyString(), eq("USER_REGISTERED"), anyString());

        assertThat(poller.publishBatch()).isZero();

        assertThat(first.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(first.getAttempts()).isEqualTo(1);
        assertThat(first.getLastError()).isEqualTo("broker down");
        assertThat(second.getStatus()).isEqualTo(OutboxStatus.PENDING);
        verify(publisher, never()).publish(anyString(), eq("USER_UPDATED"), anyString());
    }

    @Test
    void permanentlyRejectedRowIsMarkedFailedAndBatchContinues() {
        UserOutboxEntity poison = row("USER_REGISTERED");
        UserOutboxEntity next = row("USER_UPDATED");
        when(repository.lockPendingBatch(100)).thenReturn(List.of(poison, next));
        doThrow(new NonRetryablePublishException("too large", null))
                .when(publisher).publish(anyString(), eq("USER_REGISTERED"), anyString());

        assertThat(poller.publishBatch()).isEqualTo(1);

        assertThat(poison.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(next.getStatus()).isEqualTo(OutboxStatus.SENT);
    }

    private static UserOutboxEntity row(String type) {
        UserOutboxEntity row = new UserOutboxEntity();
        row.setAggregateId(UUID.randomUUID());
        row.setEventType(type);
        row.setPayload("{}");
        return row;
    }
}
