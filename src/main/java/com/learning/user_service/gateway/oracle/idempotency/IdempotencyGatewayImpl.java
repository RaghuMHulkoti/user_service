package com.learning.user_service.gateway.oracle.idempotency;

import com.learning.user_service.gateway.IdempotencyGateway;
import com.learning.user_service.gateway.oracle.idempotency.entity.IdempotencyKeyId;
import com.learning.user_service.gateway.oracle.idempotency.repository.IdempotencyKeyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class IdempotencyGatewayImpl implements IdempotencyGateway {

    private final IdempotencyKeyRepository repository;

    public IdempotencyGatewayImpl(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean claim(String scope, String key, String requestHash) {
        try {
            repository.claim(scope, key, requestHash);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    @Override
    public void complete(String scope, String key, String responseBody) {
        repository.complete(scope, key, responseBody);
    }

    @Override
    public void release(String scope, String key) {
        repository.release(scope, key);
    }

    @Override
    public void deleteExpired(String scope, String key, Instant cutoff) {
        repository.deleteExpired(scope, key, cutoff);
    }

    @Override
    public int deleteCreatedBefore(Instant cutoff) {
        return repository.deleteCreatedBefore(cutoff);
    }

    @Override
    public Optional<StoredKey> find(String scope, String key) {
        return repository.findById(new IdempotencyKeyId(scope, key))
                .map(k -> new StoredKey(k.getRequestHash(), k.getStatus(), k.getResponseBody()));
    }
}
