package com.learning.user_service.infrastructure.messaging;

import com.learning.user_service.domain.port.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default publisher: writes events to the log. Lets the service run without a broker. */
@Component
@ConditionalOnProperty(name = "app.events.publisher", havingValue = "log", matchIfMissing = true)
public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(String key, String eventType, String payloadJson) {
        log.info("EVENT {} key={} payload={}", eventType, key, payloadJson);
    }
}
