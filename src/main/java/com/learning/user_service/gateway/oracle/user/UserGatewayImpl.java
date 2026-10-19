package com.learning.user_service.gateway.oracle.user;

import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.gateway.oracle.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class UserGatewayImpl implements UserGateway {

    private final com.learning.user_service.gateway.oracle.user.repository.UserRepository repository;

    public UserGatewayImpl(com.learning.user_service.gateway.oracle.user.repository.UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserEntity> findById(UUID userId) {
        return repository.findById(userId);
    }

    @Override
    public Optional<UserEntity> findActiveByEmail(String email) {
        return repository.findActiveByEmail(email);
    }

    @Override
    public boolean existsActiveByEmail(String email) {
        return repository.existsActiveByEmail(email);
    }

    @Override
    public boolean existsActiveByPhone(String phone) {
        return repository.existsActiveByPhone(phone);
    }

    @Override
    public Page<UserEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<UserEntity> findByStatus(UserStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    @Override
    public UserEntity saveAndFlush(UserEntity user) {
        return repository.saveAndFlush(user);
    }

    @Override
    public void flush() {
        repository.flush();
    }

    @Override
    public void recordLogin(UUID userId, Instant at) {
        repository.recordLogin(userId, at);
    }
}
