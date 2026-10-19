package com.learning.user_service.core.support;

import com.learning.user_service.config.AppProperties;
import com.learning.user_service.gateway.IdempotencyGateway;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * {@code Idempotency-Key} support (TDD §11.1, §20.5).
 * <ol>
 *   <li>First call: claim the key (IN_PROGRESS), run the action, store the response (COMPLETED).</li>
 *   <li>Same key + same body: return the stored response; the action does not run again.</li>
 *   <li>Same key + different body: 422. Same key while the first call still runs: 409.</li>
 * </ol>
 * Keys are scoped (per user, per endpoint) and kept for 24h.
 */
@Component
public class IdempotencyService {

    private static final int MAX_KEY_LENGTH = 100;

    private final IdempotencyGateway gateway;
    private final JsonMapper jsonMapper;
    private final Duration ttl;
    private final Clock clock;

    public IdempotencyService(IdempotencyGateway gateway, JsonMapper jsonMapper, AppProperties properties,
                              Clock clock) {
        this.gateway = gateway;
        this.jsonMapper = jsonMapper;
        this.ttl = properties.idempotency().ttl();
        this.clock = clock;
    }

    public <T> Outcome<T> execute(String scope, String key, Object request, Class<T> responseType, Supplier<T> action) {
        if (key == null || key.isBlank()) {
            return new Outcome<>(action.get(), false);
        }
        if (key.length() > MAX_KEY_LENGTH) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Idempotency-Key must be at most 100 characters");
        }
        String requestHash = SecureTokens.sha256Hex(jsonMapper.writeValueAsString(request));

        deleteIfExpired(scope, key);
        if (claim(scope, key, requestHash)) {
            return runAndStore(scope, key, responseType, action);
        }
        return replay(scope, key, requestHash, responseType);
    }

    /** Inserts the IN_PROGRESS row. A duplicate (scope, key) means another call already claimed it. */
    private boolean claim(String scope, String key, String requestHash) {
        return gateway.claim(scope, key, requestHash);
    }

    private <T> Outcome<T> runAndStore(String scope, String key, Class<T> responseType, Supplier<T> action) {
        T result;
        try {
            result = action.get();
        } catch (RuntimeException ex) {
            // Failed calls do not burn the key: the client may retry with it.
            gateway.release(scope, key);
            throw ex;
        }
        gateway.complete(scope, key, jsonMapper.writeValueAsString(result));
        return new Outcome<>(result, false);
    }

    private <T> Outcome<T> replay(String scope, String key, String requestHash, Class<T> responseType) {
        IdempotencyGateway.StoredKey stored = gateway.find(scope, key).orElseThrow(() -> new ApiException(
                ErrorCode.IDEMPOTENCY_IN_PROGRESS, "Request with this Idempotency-Key was just cancelled; retry"));
        if (!requestHash.equals(stored.requestHash())) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_KEY_REUSED,
                    "Idempotency-Key was already used with a different request body");
        }
        if (!"COMPLETED".equals(stored.status())) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_IN_PROGRESS,
                    "A request with this Idempotency-Key is still being processed");
        }
        return new Outcome<>(jsonMapper.readValue(stored.responseBody(), responseType), true);
    }

    private void deleteIfExpired(String scope, String key) {
        gateway.deleteExpired(scope, key, clock.instant().minus(ttl));
    }

    public record Outcome<T>(T body, boolean replayed) {
    }
}
