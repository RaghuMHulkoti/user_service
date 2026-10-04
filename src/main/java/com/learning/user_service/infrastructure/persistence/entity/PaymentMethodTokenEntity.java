package com.learning.user_service.infrastructure.persistence.entity;

import com.learning.user_service.domain.model.PaymentProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** A PSP token (e.g. Stripe {@code pm_xxx}). Never a raw card number. */
@Entity
@Table(name = "payment_method_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    @Column(name = "provider_token", nullable = false)
    private String providerToken;

    private String brand;

    private String last4;

    @Column(name = "exp_month")
    private Short expMonth;

    @Column(name = "exp_year")
    private Short expYear;

    @Column(name = "is_default", nullable = false)
    private boolean defaultMethod;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
