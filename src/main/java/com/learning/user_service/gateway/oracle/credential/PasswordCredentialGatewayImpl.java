package com.learning.user_service.gateway.oracle.credential;

import com.learning.user_service.gateway.PasswordCredentialGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import com.learning.user_service.gateway.oracle.credential.repository.PasswordCredentialRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PasswordCredentialGatewayImpl implements PasswordCredentialGateway {

    private final com.learning.user_service.gateway.oracle.credential.repository.PasswordCredentialRepository repository;

    public PasswordCredentialGatewayImpl(com.learning.user_service.gateway.oracle.credential.repository.PasswordCredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PasswordCredentialEntity> findById(UUID userId) {
        return repository.findById(userId);
    }

    @Override
    public PasswordCredentialEntity save(PasswordCredentialEntity credential) {
        return repository.save(credential);
    }

    @Override
    public void deleteById(UUID userId) {
        repository.deleteById(userId);
    }
}
