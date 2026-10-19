package com.learning.user_service.gateway.oracle.credential;

import com.learning.user_service.gateway.PasswordResetTokenGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordResetTokenEntity;
import com.learning.user_service.gateway.oracle.credential.repository.PasswordResetTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenGatewayImpl implements PasswordResetTokenGateway {

    private final com.learning.user_service.gateway.oracle.credential.repository.PasswordResetTokenRepository repository;

    public PasswordResetTokenGatewayImpl(com.learning.user_service.gateway.oracle.credential.repository.PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }

    @Override
    public PasswordResetTokenEntity save(PasswordResetTokenEntity token) {
        return repository.save(token);
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
