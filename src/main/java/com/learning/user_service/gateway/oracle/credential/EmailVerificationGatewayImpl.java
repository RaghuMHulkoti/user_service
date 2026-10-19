package com.learning.user_service.gateway.oracle.credential;

import com.learning.user_service.gateway.EmailVerificationGateway;
import com.learning.user_service.gateway.oracle.credential.entity.EmailVerificationEntity;
import com.learning.user_service.gateway.oracle.credential.repository.EmailVerificationRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationGatewayImpl implements EmailVerificationGateway {

    private final com.learning.user_service.gateway.oracle.credential.repository.EmailVerificationRepository repository;

    public EmailVerificationGatewayImpl(com.learning.user_service.gateway.oracle.credential.repository.EmailVerificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EmailVerificationEntity> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }

    @Override
    public EmailVerificationEntity save(EmailVerificationEntity verification) {
        return repository.save(verification);
    }

    @Override
    public void consumeAllOpen(UUID userId, Instant now) {
        repository.consumeAllOpen(userId, now);
    }

    @Override
    public int deleteExpiredBefore(Instant cutoff) {
        return repository.deleteExpiredBefore(cutoff);
    }
}
