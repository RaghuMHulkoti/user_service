package com.learning.user_service.gateway.oracle.idempotency.repository;

import com.learning.user_service.gateway.oracle.idempotency.entity.IdempotencyKeyEntity;
import com.learning.user_service.gateway.oracle.idempotency.entity.IdempotencyKeyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, IdempotencyKeyId> {

    /** Plain INSERT: a duplicate (scope, key) must fail, which save() on an assigned id would not do. */
    @Transactional
    @Modifying
    @Query(value = "insert into {h-schema}idempotency_keys (scope, idem_key, request_hash, status) "
            + "values (:scope, :key, :requestHash, 'IN_PROGRESS')", nativeQuery = true)
    int claim(String scope, String key, String requestHash);

    @Transactional
    @Modifying
    @Query("update IdempotencyKeyEntity k set k.status = 'COMPLETED', k.responseStatus = 200, "
            + "k.responseBody = :body where k.scope = :scope and k.key = :key")
    int complete(String scope, String key, String body);

    @Transactional
    @Modifying
    @Query("delete from IdempotencyKeyEntity k where k.scope = :scope and k.key = :key")
    int release(String scope, String key);

    @Transactional
    @Modifying
    @Query("delete from IdempotencyKeyEntity k where k.scope = :scope and k.key = :key and k.createdAt < :cutoff")
    int deleteExpired(String scope, String key, Instant cutoff);

    @Transactional
    @Modifying
    @Query("delete from IdempotencyKeyEntity k where k.createdAt < :cutoff")
    int deleteCreatedBefore(Instant cutoff);
}
