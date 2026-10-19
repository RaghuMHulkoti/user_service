package com.learning.user_service.gateway.redis;

import com.learning.user_service.gateway.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

/**
 * Fixed-window counter in Redis: {@code ratelimit:{key}:{window}}.
 * If Redis is down it fails open (logs a warning) so login keeps working; account
 * lockout in the DB still protects against brute force.
 */
@Component
public class RedisRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);

    private final StringRedisTemplate redis;
    private final Clock clock;

    public RedisRateLimiter(StringRedisTemplate redis, Clock clock) {
        this.redis = redis;
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String key, int limit, Duration window) {
        long windowSeconds = window.toSeconds();
        long bucket = clock.instant().getEpochSecond() / windowSeconds;
        String redisKey = "ratelimit:" + key + ":" + bucket;
        try {
            Long count = redis.opsForValue().increment(redisKey);
            if (count != null && count == 1L) {
                redis.expire(redisKey, window);
            }
            return count == null || count <= limit;
        } catch (RuntimeException ex) {
            log.warn("Rate limiter unavailable, allowing request: {}", ex.getMessage());
            return true;
        }
    }
}
