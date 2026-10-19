package com.learning.user_service.gateway.oracle.refreshtoken.repository;

import com.learning.user_service.gateway.oracle.refreshtoken.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    /*
     * Each delete runs in its own transaction: revoking all sessions is called from an after-commit hook (password
     * change or reset), where the finished transaction can no longer be joined.
     */

    /** The delete count is how a token is consumed exactly once: of two concurrent callers only one sees 1. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("delete from RefreshTokenEntity t where t.jti = :jti")
    int deleteByJti(String jti);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("delete from RefreshTokenEntity t where t.userId = :userId")
    int deleteByUserId(UUID userId);

    @Transactional
    @Modifying
    @Query("delete from RefreshTokenEntity t where t.expiresAt < :cutoff")
    int deleteExpiredBefore(Instant cutoff);
}
