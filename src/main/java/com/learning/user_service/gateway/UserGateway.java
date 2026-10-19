package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.core.domain.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserGateway {

    Optional<UserEntity> findById(UUID userId);

    Optional<UserEntity> findActiveByEmail(String email);

    boolean existsActiveByEmail(String email);

    boolean existsActiveByPhone(String phone);

    Page<UserEntity> findAll(Pageable pageable);

    Page<UserEntity> findByStatus(UserStatus status, Pageable pageable);

    UserEntity saveAndFlush(UserEntity user);

    void flush();

    /** Bulk update so a login does not bump the optimistic-lock version of the profile. */
    void recordLogin(UUID userId, Instant at);
}
