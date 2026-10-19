package com.learning.user_service.gateway.oracle.user.repository;

import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // activeEmailKey is NULL for deleted users and carries the unique index, so these lookups use it
    @Query("select u from UserEntity u where u.activeEmailKey = lower(:email)")
    Optional<UserEntity> findActiveByEmail(String email);

    @Query("select count(u) > 0 from UserEntity u where u.activeEmailKey = lower(:email)")
    boolean existsActiveByEmail(String email);

    @Query("select count(u) > 0 from UserEntity u where u.activePhoneKey = :phone")
    boolean existsActiveByPhone(String phone);

    /** Bulk update so a login does not bump the optimistic-lock version of the profile. */
    @Modifying
    @Query("update UserEntity u set u.lastLoginAt = :at where u.id = :id")
    int recordLogin(UUID id, Instant at);

    Page<UserEntity> findByStatus(UserStatus status, Pageable pageable);
}
