package com.learning.user_service.gateway.oracle.user.entity;

import com.learning.user_service.core.domain.model.RoleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor
public class RoleEntity {

    @Id
    private Short id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleCode code;

    @Column(nullable = false)
    private String label;

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof RoleEntity other && Objects.equals(id, other.id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
