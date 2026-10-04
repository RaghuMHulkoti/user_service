package com.learning.user_service.domain.port;

import java.time.Duration;

/** Fixed-window counter. */
public interface RateLimiter {

    /**
     * Counts one hit for the key.
     *
     * @return true when the hit is within the limit
     */
    boolean tryAcquire(String key, int limit, Duration window);
}
