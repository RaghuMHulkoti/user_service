package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.credential.entity.PasswordResetTokenEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenGateway {

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    PasswordResetTokenEntity save(PasswordResetTokenEntity token);

    /** Marks every open token of the user as used, so only the newest one works. */
    void consumeAllOpen(UUID userId, Instant now);

    int deleteExpiredBefore(Instant cutoff);
}
