package com.learning.user_service.gateway.oracle.paymentmethod;

import com.learning.user_service.gateway.PaymentMethodGateway;
import com.learning.user_service.gateway.oracle.paymentmethod.entity.PaymentMethodTokenEntity;
import com.learning.user_service.gateway.oracle.paymentmethod.repository.PaymentMethodTokenRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodGatewayImpl implements PaymentMethodGateway {

    private final com.learning.user_service.gateway.oracle.paymentmethod.repository.PaymentMethodTokenRepository repository;

    public PaymentMethodGatewayImpl(com.learning.user_service.gateway.oracle.paymentmethod.repository.PaymentMethodTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PaymentMethodTokenEntity> findByUserId(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Override
    public Optional<PaymentMethodTokenEntity> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId);
    }

    @Override
    public boolean existsByUserIdAndProviderToken(UUID userId, String providerToken) {
        return repository.existsByUserIdAndProviderToken(userId, providerToken);
    }

    @Override
    public long countByUserId(UUID userId) {
        return repository.countByUserId(userId);
    }

    @Override
    public Optional<PaymentMethodTokenEntity> findFirstByUserId(UUID userId) {
        return repository.findFirstByUserIdOrderByCreatedAtAsc(userId);
    }

    @Override
    public void clearDefault(UUID userId) {
        repository.clearDefault(userId);
    }

    @Override
    public PaymentMethodTokenEntity saveAndFlush(PaymentMethodTokenEntity method) {
        return repository.saveAndFlush(method);
    }

    @Override
    public void delete(PaymentMethodTokenEntity method) {
        repository.delete(method);
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }
}
