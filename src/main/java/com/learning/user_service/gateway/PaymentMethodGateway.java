package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodGateway {

    List<PaymentMethodTokenEntity> findByUserId(UUID userId);

    Optional<PaymentMethodTokenEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndProviderToken(UUID userId, String providerToken);

    long countByUserId(UUID userId);

    Optional<PaymentMethodTokenEntity> findFirstByUserId(UUID userId);

    void clearDefault(UUID userId);

    PaymentMethodTokenEntity saveAndFlush(PaymentMethodTokenEntity method);

    void delete(PaymentMethodTokenEntity method);

    void flush();

    void deleteByUserId(UUID userId);
}
