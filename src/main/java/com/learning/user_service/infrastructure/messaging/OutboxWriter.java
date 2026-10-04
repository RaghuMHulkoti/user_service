package com.learning.user_service.infrastructure.messaging;

import com.learning.user_service.domain.event.EventEnvelope;
import com.learning.user_service.domain.event.UserEventType;
import com.learning.user_service.domain.port.DomainEventRecorder;
import com.learning.user_service.infrastructure.persistence.entity.UserOutboxEntity;
import com.learning.user_service.infrastructure.persistence.repository.UserOutboxRepository;
import com.learning.user_service.infrastructure.web.RequestIdFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Transactional outbox (TDD §13.8). Writes the event in the SAME transaction as the state
 * change; {@link OutboxPoller} publishes it later. No dual write.
 */
@Component
public class OutboxWriter implements DomainEventRecorder {

    private final UserOutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public OutboxWriter(UserOutboxRepository outboxRepository, JsonMapper jsonMapper, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID record(UUID aggregateId, UserEventType type, Map<String, Object> data) {
        UUID eventId = UUID.randomUUID();
        String traceId = MDC.get(RequestIdFilter.MDC_TRACE_ID);
        EventEnvelope envelope = new EventEnvelope(eventId, type.name(), Instant.now(clock),
                EventEnvelope.PRODUCER, EventEnvelope.SCHEMA_VERSION, traceId, data);

        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("eventId", eventId.toString());
        headers.put("eventType", type.name());
        if (traceId != null) {
            headers.put("traceId", traceId);
        }

        UserOutboxEntity row = new UserOutboxEntity();
        row.setAggregateId(aggregateId);
        row.setEventType(type.name());
        row.setPayload(jsonMapper.writeValueAsString(envelope));
        row.setHeaders(jsonMapper.writeValueAsString(headers));
        outboxRepository.save(row);
        return eventId;
    }
}
