package com.learning.user_service.application.support;

import com.learning.user_service.infrastructure.config.AppProperties;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.sql.Timestamp;
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

    private final JdbcTemplate jdbc;
    private final JsonMapper jsonMapper;
    private final Duration ttl;
    private final Clock clock;

    public IdempotencyService(JdbcTemplate jdbc, JsonMapper jsonMapper, AppProperties properties, Clock clock) {
        this.jdbc = jdbc;
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
        int claimed = jdbc.update("insert into idempotency_keys (scope, idem_key, request_hash, status) "
                + "values (?, ?, ?, 'IN_PROGRESS') on conflict do nothing", scope, key, requestHash);
        if (claimed == 1) {
            return runAndStore(scope, key, responseType, action);
        }
        return replay(scope, key, requestHash, responseType);
    }

    private <T> Outcome<T> runAndStore(String scope, String key, Class<T> responseType, Supplier<T> action) {
        T result;
        try {
            result = action.get();
        } catch (RuntimeException ex) {
            // Failed calls do not burn the key: the client may retry with it.
            jdbc.update("delete from idempotency_keys where scope = ? and idem_key = ?", scope, key);
            throw ex;
        }
        jdbc.update("update idempotency_keys set status = 'COMPLETED', response_status = 200, response_body = ? "
                + "where scope = ? and idem_key = ?", jsonMapper.writeValueAsString(result), scope, key);
        return new Outcome<>(result, false);
    }

    private <T> Outcome<T> replay(String scope, String key, String requestHash, Class<T> responseType) {
        StoredKey stored;
        try {
            stored = jdbc.queryForObject("select request_hash, status, response_body from idempotency_keys "
                            + "where scope = ? and idem_key = ?",
                    (rs, i) -> new StoredKey(rs.getString(1), rs.getString(2), rs.getString(3)), scope, key);
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_IN_PROGRESS, "Request with this Idempotency-Key was just "
                    + "cancelled; retry");
        }
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
        jdbc.update("delete from idempotency_keys where scope = ? and idem_key = ? and created_at < ?",
                scope, key, Timestamp.from(clock.instant().minus(ttl)));
    }

    public record Outcome<T>(T body, boolean replayed) {
    }

    private record StoredKey(String requestHash, String status, String responseBody) {
    }
}
