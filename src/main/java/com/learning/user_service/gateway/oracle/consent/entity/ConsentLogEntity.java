package com.learning.user_service.gateway.oracle.consent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.type.NumericBooleanConverter;

import java.time.Instant;
import java.util.UUID;

/** Append-only consent history. The latest row per (user, kind) is the current state. */
@Entity
@Table(name = "consent_log")
@Getter
@Setter
@NoArgsConstructor
public class ConsentLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String kind;

    @Column(nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean granted;

    private String ip;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
