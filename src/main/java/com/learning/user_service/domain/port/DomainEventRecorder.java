package com.learning.user_service.domain.port;

import com.learning.user_service.domain.event.UserEventType;

import java.util.Map;
import java.util.UUID;

/** Records a domain event inside the caller's transaction (implemented by the outbox). */
public interface DomainEventRecorder {

    UUID record(UUID aggregateId, UserEventType type, Map<String, Object> data);
}
