package com.learning.user_service.gateway.oracle.credential.repository;

import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordCredentialRepository extends JpaRepository<PasswordCredentialEntity, UUID> {
}
