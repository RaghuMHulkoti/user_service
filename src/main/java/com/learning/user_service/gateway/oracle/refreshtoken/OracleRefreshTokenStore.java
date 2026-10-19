package com.learning.user_service.gateway.oracle.refreshtoken;

import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.oracle.refreshtoken.entity.RefreshTokenEntity;
import com.learning.user_service.gateway.oracle.refreshtoken.repository.RefreshTokenRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh tokens in the USERS schema, so login does not depend on Redis. This is the default;
 * {@code app.refresh-token-store=redis} switches back to {@code RedisRefreshTokenStore}.
 */
@Component
@ConditionalOnProperty(name = "app.refresh-token-store", havingValue = "oracle", matchIfMissing = true)
public class OracleRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;
    private final Clock clock;

    public OracleRefreshTokenStore(RefreshTokenRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void save(String jti, UUID userId, String secretHash, Duration ttl) {
        repository.save(new RefreshTokenEntity(jti, userId, secretHash, Instant.now(clock).plus(ttl)));
    }

    @Override
    public Optional<StoredRefreshToken> find(String jti) {
        return repository.findById(jti).filter(this::live).map(OracleRefreshTokenStore::stored);
    }

    @Override
    public Optional<StoredRefreshToken> consume(String jti) {
        var found = repository.findById(jti);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        // only the caller whose delete removes the row gets the token, so a token works once
        boolean won = repository.deleteByJti(jti) == 1;
        return won && live(found.get()) ? Optional.of(stored(found.get())) : Optional.empty();
    }

    @Override
    public void revoke(String jti) {
        repository.deleteByJti(jti);
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        repository.deleteByUserId(userId);
    }

    @Override
    public int deleteExpired(Instant cutoff) {
        return repository.deleteExpiredBefore(cutoff);
    }

    private boolean live(RefreshTokenEntity token) {
        return token.getExpiresAt().isAfter(Instant.now(clock));
    }

    private static StoredRefreshToken stored(RefreshTokenEntity e) {
        return new StoredRefreshToken(e.getJti(), e.getUserId(), e.getSecretHash());
    }
}
