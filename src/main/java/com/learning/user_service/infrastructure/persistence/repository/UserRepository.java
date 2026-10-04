package com.learning.user_service.infrastructure.persistence.repository;

import com.learning.user_service.domain.model.UserStatus;
import com.learning.user_service.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @Query("select u from UserEntity u where lower(u.email) = lower(:email) and u.deletedAt is null")
    Optional<UserEntity> findActiveByEmail(String email);

    @Query("select count(u) > 0 from UserEntity u where lower(u.email) = lower(:email) and u.deletedAt is null")
    boolean existsActiveByEmail(String email);

    @Query("select count(u) > 0 from UserEntity u where u.phone = :phone and u.deletedAt is null")
    boolean existsActiveByPhone(String phone);

    /** Bulk update so a login does not bump the optimistic-lock version of the profile. */
    @Modifying
    @Query("update UserEntity u set u.lastLoginAt = :at where u.id = :id")
    int recordLogin(UUID id, Instant at);

    Page<UserEntity> findByStatus(UserStatus status, Pageable pageable);
}
