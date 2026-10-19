package com.learning.user_service.gateway.oracle.user.repository;

import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.gateway.oracle.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Short> {

    Optional<RoleEntity> findByCode(RoleCode code);

    List<RoleEntity> findByCodeIn(Collection<RoleCode> codes);
}
