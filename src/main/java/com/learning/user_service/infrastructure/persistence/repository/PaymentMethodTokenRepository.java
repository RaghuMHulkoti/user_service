package com.learning.user_service.infrastructure.persistence.repository;

import com.learning.user_service.infrastructure.persistence.entity.PaymentMethodTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodTokenRepository extends JpaRepository<PaymentMethodTokenEntity, UUID> {

    List<PaymentMethodTokenEntity> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Optional<PaymentMethodTokenEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndProviderToken(UUID userId, String providerToken);

    long countByUserId(UUID userId);

    Optional<PaymentMethodTokenEntity> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PaymentMethodTokenEntity p set p.defaultMethod = false "
            + "where p.userId = :userId and p.defaultMethod = true")
    int clearDefault(UUID userId);

    void deleteByUserId(UUID userId);
}
