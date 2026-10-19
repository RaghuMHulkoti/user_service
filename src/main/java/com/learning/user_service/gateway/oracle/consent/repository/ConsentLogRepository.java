package com.learning.user_service.gateway.oracle.consent.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ConsentLogRepository extends JpaRepository<ConsentLogEntity, Long> {

    /** Latest decision per consent kind (the highest id is the newest row). */
    @Query("select c from ConsentLogEntity c where c.userId = :userId and c.id = "
            + "(select max(c2.id) from ConsentLogEntity c2 where c2.userId = c.userId and c2.kind = c.kind) "
            + "order by c.kind")
    List<ConsentLogEntity> findLatestPerKind(UUID userId);

    List<ConsentLogEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Transactional
    @Modifying
    @Query("update ConsentLogEntity c set c.ip = null where c.userId = :userId")
    int clearIpByUserId(UUID userId);
}
