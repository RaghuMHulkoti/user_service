package com.learning.user_service.application.query;

import com.learning.user_service.domain.model.PaymentProvider;
import com.learning.user_service.infrastructure.persistence.entity.PaymentMethodTokenEntity;

import java.time.Instant;
import java.util.UUID;

/** Never exposes the provider token itself; only display data. */
public record PaymentMethodView(UUID id, PaymentProvider provider, String brand, String last4,
                                Short expMonth, Short expYear, boolean isDefault, Instant createdAt) {

    public static PaymentMethodView from(PaymentMethodTokenEntity p) {
        return new PaymentMethodView(p.getId(), p.getProvider(), p.getBrand(), p.getLast4(),
                p.getExpMonth(), p.getExpYear(), p.isDefaultMethod(), p.getCreatedAt());
    }
}
