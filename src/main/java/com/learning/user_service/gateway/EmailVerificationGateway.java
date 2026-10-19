package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.credential.entity.EmailVerificationEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationGateway {

    Optional<EmailVerificationEntity> findByTokenHash(String tokenHash);

    EmailVerificationEntity save(EmailVerificationEntity verification);

    /** Marks every open token of the user as used, so only the newest one works. */
    void consumeAllOpen(UUID userId, Instant now);

    int deleteExpiredBefore(Instant cutoff);
}
