package com.learning.user_service.gateway.oracle.credential.repository;

import org.springframework.transaction.annotation.Transactional;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PasswordResetTokenEntity t set t.consumedAt = :now where t.userId = :userId and t.consumedAt is null")
    int consumeAllOpen(UUID userId, Instant now);

    @Transactional
    @Modifying
    @Query("delete from PasswordResetTokenEntity e where e.expiresAt < :cutoff")
    int deleteExpiredBefore(Instant cutoff);
}
