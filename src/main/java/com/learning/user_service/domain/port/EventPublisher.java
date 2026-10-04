package com.learning.user_service.domain.port;

/** Outbound port used by the outbox poller to hand events to the broker. */
public interface EventPublisher {

    /**
     * Publishes one event. Must block until the broker acknowledges it.
     *
     * @throws NonRetryablePublishException when the broker will never accept this event
     * @throws RuntimeException for any other (retryable) failure
     */
    void publish(String key, String eventType, String payloadJson);
}
