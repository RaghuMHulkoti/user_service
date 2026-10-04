package com.learning.user_service.infrastructure.persistence.repository;

import com.learning.user_service.infrastructure.persistence.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, UUID> {

    Optional<EmailVerificationEntity> findByTokenHash(String tokenHash);

    /** Marks every open token of the user as used, so only the newest one works. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update EmailVerificationEntity e set e.consumedAt = :now where e.userId = :userId and e.consumedAt is null")
    int consumeAllOpen(UUID userId, Instant now);
}
