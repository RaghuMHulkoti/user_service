package com.learning.user_service.domain.port;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/** Stores opaque refresh tokens (hashed) keyed by their jti. */
public interface RefreshTokenStore {

    void save(String jti, UUID userId, String secretHash, Duration ttl);

    Optional<StoredRefreshToken> find(String jti);

    /** Reads and deletes in one atomic step, so a token can be used only once. */
    Optional<StoredRefreshToken> consume(String jti);

    void revoke(String jti);

    void revokeAllForUser(UUID userId);

    record StoredRefreshToken(String jti, UUID userId, String secretHash) {
    }
}
