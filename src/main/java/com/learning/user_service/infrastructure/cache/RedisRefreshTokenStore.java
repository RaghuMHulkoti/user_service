package com.learning.user_service.infrastructure.cache;

import com.learning.user_service.domain.port.RefreshTokenStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Refresh tokens in Redis (TDD §15.1, §16.2).
 * <ul>
 *   <li>{@code session:refresh:{jti}} → {@code userId|sha256(secret)}, TTL 30d</li>
 *   <li>{@code session:user:{userId}} → set of jti, so all sessions can be revoked at once</li>
 * </ul>
 */
@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String TOKEN_PREFIX = "session:refresh:";
    private static final String USER_PREFIX = "session:user:";
    private static final String SEPARATOR = "|";

    private final StringRedisTemplate redis;

    public RedisRefreshTokenStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void save(String jti, UUID userId, String secretHash, Duration ttl) {
        redis.opsForValue().set(TOKEN_PREFIX + jti, userId + SEPARATOR + secretHash, ttl);
        String userKey = USER_PREFIX + userId;
        redis.opsForSet().add(userKey, jti);
        redis.expire(userKey, ttl);
    }

    @Override
    public Optional<StoredRefreshToken> find(String jti) {
        return parse(jti, redis.opsForValue().get(TOKEN_PREFIX + jti));
    }

    @Override
    public Optional<StoredRefreshToken> consume(String jti) {
        Optional<StoredRefreshToken> token = parse(jti, redis.opsForValue().getAndDelete(TOKEN_PREFIX + jti));
        token.ifPresent(t -> redis.opsForSet().remove(USER_PREFIX + t.userId(), jti));
        return token;
    }

    private static Optional<StoredRefreshToken> parse(String jti, String value) {
        if (value == null) {
            return Optional.empty();
        }
        int split = value.indexOf(SEPARATOR);
        if (split < 0) {
            return Optional.empty();
        }
        UUID userId = UUID.fromString(value.substring(0, split));
        return Optional.of(new StoredRefreshToken(jti, userId, value.substring(split + 1)));
    }

    @Override
    public void revoke(String jti) {
        find(jti).ifPresent(token -> redis.opsForSet().remove(USER_PREFIX + token.userId(), jti));
        redis.delete(TOKEN_PREFIX + jti);
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        String userKey = USER_PREFIX + userId;
        Set<String> jtis = redis.opsForSet().members(userKey);
        if (jtis != null && !jtis.isEmpty()) {
            redis.delete(jtis.stream().map(jti -> TOKEN_PREFIX + jti).toList());
        }
        redis.delete(userKey);
    }
}
