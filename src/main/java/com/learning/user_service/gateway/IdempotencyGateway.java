package com.learning.user_service.gateway;

import java.time.Instant;
import java.util.Optional;

public interface IdempotencyGateway {

    /** Inserts the IN_PROGRESS row. Returns false when another call already claimed (scope, key). */
    boolean claim(String scope, String key, String requestHash);

    void complete(String scope, String key, String responseBody);

    void release(String scope, String key);

    void deleteExpired(String scope, String key, Instant cutoff);

    int deleteCreatedBefore(Instant cutoff);

    Optional<StoredKey> find(String scope, String key);

    record StoredKey(String requestHash, String status, String responseBody) {
    }
}
