package com.learning.user_service.gateway.oracle.consent;

import com.learning.user_service.gateway.ConsentGateway;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import com.learning.user_service.gateway.oracle.consent.repository.ConsentLogRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConsentGatewayImpl implements ConsentGateway {

    private final ConsentLogRepository repository;

    public ConsentGatewayImpl(ConsentLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ConsentLogEntity> findLatestPerKind(UUID userId) {
        return repository.findLatestPerKind(userId);
    }

    @Override
    public List<ConsentLogEntity> findHistory(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public ConsentLogEntity saveAndFlush(ConsentLogEntity consent) {
        return repository.saveAndFlush(consent);
    }

    @Override
    public void clearIpByUserId(UUID userId) {
        repository.clearIpByUserId(userId);
    }
}
