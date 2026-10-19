package com.learning.user_service.gateway.oracle.refreshtoken.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenEntity {

    @Id
    @Column(name = "jti", nullable = false)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "secret_hash", nullable = false)
    private String secretHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RefreshTokenEntity(String jti, UUID userId, String secretHash, Instant expiresAt) {
        this.jti = jti;
        this.userId = userId;
        this.secretHash = secretHash;
        this.expiresAt = expiresAt;
    }
}
