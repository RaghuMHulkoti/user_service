package com.learning.user_service.infrastructure.persistence.repository;

import com.learning.user_service.domain.model.RoleCode;
import com.learning.user_service.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Short> {

    Optional<RoleEntity> findByCode(RoleCode code);

    List<RoleEntity> findByCodeIn(Collection<RoleCode> codes);
}
