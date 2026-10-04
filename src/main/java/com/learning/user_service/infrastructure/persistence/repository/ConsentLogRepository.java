package com.learning.user_service.infrastructure.persistence.repository;

import com.learning.user_service.infrastructure.persistence.entity.ConsentLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ConsentLogRepository extends JpaRepository<ConsentLogEntity, Long> {

    /** Latest decision per consent kind. */
    @Query(value = "select distinct on (kind) id, user_id, kind, granted, host(ip) as ip, created_at "
            + "from consent_log where user_id = :userId order by kind, created_at desc, id desc",
            nativeQuery = true)
    List<ConsentLogEntity> findLatestPerKind(UUID userId);

    List<ConsentLogEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
