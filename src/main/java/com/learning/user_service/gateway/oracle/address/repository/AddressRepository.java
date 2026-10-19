package com.learning.user_service.gateway.oracle.address.repository;

import com.learning.user_service.gateway.oracle.address.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

    List<AddressEntity> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<AddressEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);

    Optional<AddressEntity> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);

    /**
     * Clears the current default. Runs as a bulk UPDATE so it hits the DB before the
     * new default row is written (the partial unique index allows one default per user).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AddressEntity a set a.defaultAddress = false, a.version = a.version + 1 "
            + "where a.userId = :userId and a.defaultAddress = true")
    int clearDefault(UUID userId);
}
