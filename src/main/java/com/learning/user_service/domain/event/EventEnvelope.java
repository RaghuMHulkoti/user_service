package com.learning.user_service.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Standard event envelope (TDD §13.3). */
public record EventEnvelope(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        String producer,
        int version,
        String traceId,
        Map<String, Object> data) {

    public static final String PRODUCER = "user-service";
    public static final int SCHEMA_VERSION = 1;
}
