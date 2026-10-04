package com.learning.user_service.infrastructure.persistence.repository;

import com.learning.user_service.infrastructure.persistence.entity.UserOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserOutboxRepository extends JpaRepository<UserOutboxEntity, Long> {

    /** Locks a batch of pending rows. Other pollers skip them (safe with many replicas). */
    @Query(value = "select * from user_outbox where status = 'PENDING' order by id "
            + "limit :limit for update skip locked", nativeQuery = true)
    List<UserOutboxEntity> lockPendingBatch(int limit);
}
