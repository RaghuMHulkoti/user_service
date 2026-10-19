package com.learning.user_service.web.response;

import com.learning.user_service.core.domain.model.PaymentProvider;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;

import java.time.Instant;
import java.util.UUID;

/** Never exposes the provider token itself; only display data. */
public record PaymentMethodResponse(UUID id, PaymentProvider provider, String brand, String last4,
                                Short expMonth, Short expYear, boolean isDefault, Instant createdAt) {

    public static PaymentMethodResponse from(PaymentMethodTokenEntity p) {
        return new PaymentMethodResponse(p.getId(), p.getProvider(), p.getBrand(), p.getLast4(),
                p.getExpMonth(), p.getExpYear(), p.isDefaultMethod(), p.getCreatedAt());
    }
}
