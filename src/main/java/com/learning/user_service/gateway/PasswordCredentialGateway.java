package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import java.util.Optional;
import java.util.UUID;

public interface PasswordCredentialGateway {

    Optional<PasswordCredentialEntity> findById(UUID userId);

    PasswordCredentialEntity save(PasswordCredentialEntity credential);

    void deleteById(UUID userId);
}
