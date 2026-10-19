package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.user.entity.RoleEntity;
import com.learning.user_service.core.domain.model.RoleCode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleGateway {

    Optional<RoleEntity> findByCode(RoleCode code);

    List<RoleEntity> findByCodeIn(Collection<RoleCode> codes);
}
