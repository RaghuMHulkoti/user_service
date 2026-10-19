package com.learning.user_service.gateway.oracle.user;

import com.learning.user_service.gateway.RoleGateway;
import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.gateway.oracle.user.entity.RoleEntity;
import com.learning.user_service.gateway.oracle.user.repository.RoleRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RoleGatewayImpl implements RoleGateway {

    private final com.learning.user_service.gateway.oracle.user.repository.RoleRepository repository;

    public RoleGatewayImpl(com.learning.user_service.gateway.oracle.user.repository.RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RoleEntity> findByCode(RoleCode code) {
        return repository.findByCode(code);
    }

    @Override
    public List<RoleEntity> findByCodeIn(Collection<RoleCode> codes) {
        return repository.findByCodeIn(codes);
    }
}
