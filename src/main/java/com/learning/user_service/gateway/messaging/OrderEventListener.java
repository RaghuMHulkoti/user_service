package com.learning.user_service.gateway.messaging;

import com.learning.user_service.core.support.UserMailNotifier;
import com.learning.user_service.exception.NonRetryablePublishException;
import com.learning.user_service.gateway.UserGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

/**
 * Listens to {@code domain.order.events} and emails the customer when an order is confirmed.
 * A mail the provider rejects for good is logged and skipped; any other failure propagates so the
 * consumer retries. Delivery is at-least-once, so a redelivered event can send the mail twice.
 */
@Component
@ConditionalOnProperty(name = "app.events.consumer-enabled", havingValue = "true")
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final JsonMapper jsonMapper;
    private final UserGateway userGateway;
    private final UserMailNotifier mailNotifier;

    public OrderEventListener(JsonMapper jsonMapper, UserGateway userGateway, UserMailNotifier mailNotifier) {
        this.jsonMapper = jsonMapper;
        this.userGateway = userGateway;
        this.mailNotifier = mailNotifier;
    }

    @KafkaListener(topics = "${app.events.order-topic:domain.order.events}",
            groupId = "${spring.kafka.consumer.group-id:user-service}")
    public void onOrderEvent(String payload) {
        JsonNode envelope = jsonMapper.readTree(payload);
        if (!"ORDER_CONFIRMED".equals(envelope.path("eventType").asString(""))) {
            return;
        }
        JsonNode data = envelope.path("data");
        UUID customerId = UUID.fromString(data.path("customerId").asString(""));
        userGateway.findById(customerId).ifPresentOrElse(user -> {
            try {
                mailNotifier.orderConfirmed(user.getEmail(), user.getFirstName(), data);
            } catch (NonRetryablePublishException ex) {
                log.error("Order confirmation mail for {} was rejected: {}", data.path("orderNumber").asString(""), ex.getMessage());
            }
        }, () -> log.warn("Order {} confirmed for unknown user {}", data.path("orderNumber").asString(""), customerId));
    }
}
