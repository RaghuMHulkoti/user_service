package com.learning.user_service.gateway.oracle.outbox.repository;

import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import com.learning.user_service.gateway.oracle.outbox.entity.UserOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserOutboxRepository extends JpaRepository<UserOutboxEntity, Long> {

    /**
     * Locks a batch of pending rows. Other pollers skip them (safe with many replicas).
     * Oracle rejects FETCH FIRST together with FOR UPDATE, so the limit sits in a subquery.
     */
    @Query(value = "select * from {h-schema}user_outbox where id in ("
            + "select id from {h-schema}user_outbox where status = 'PENDING' order by id fetch first :limit rows only) "
            + "order by id for update skip locked", nativeQuery = true)
    List<UserOutboxEntity> lockPendingBatch(int limit);

    @Transactional
    @Modifying
    @Query("delete from UserOutboxEntity e where e.sentAt < :cutoff and e.status = com.learning.user_service.gateway.oracle.outbox.entity.OutboxStatus.SENT")
    int deleteSentBefore(Instant cutoff);
}
