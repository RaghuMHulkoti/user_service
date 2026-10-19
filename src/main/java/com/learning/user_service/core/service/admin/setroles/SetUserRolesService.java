package com.learning.user_service.core.service.admin.setroles;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.AdminUserSupport;
import com.learning.user_service.core.support.ProfileCache;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.RoleGateway;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.user.entity.RoleEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Operator tools: assign roles. */
@Component
public class SetUserRolesService implements Service<SetUserRolesServiceRequest, SetUserRolesServiceResponse> {

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final DomainEventRecorder events;
    private final ProfileCache profileCache;
    private final AdminUserSupport adminUserSupport;

    public SetUserRolesService(UserGateway userGateway, RoleGateway roleGateway, DomainEventRecorder events,
                               ProfileCache profileCache, AdminUserSupport adminUserSupport) {
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.events = events;
        this.profileCache = profileCache;
        this.adminUserSupport = adminUserSupport;
    }

    @Override
    @Transactional
    public SetUserRolesServiceResponse execute(SetUserRolesServiceRequest request) {
        UUID userId = request.getUserId();
        UserEntity user = adminUserSupport.findManageable(userId);
        List<RoleEntity> roleEntities = roleGateway.findByCodeIn(request.getRoles());
        user.setRoles(new HashSet<>(roleEntities));
        userGateway.flush();
        events.record(userId, UserEventType.USER_UPDATED, Map.of("userId", userId.toString(),
                "changes", List.of("roles"),
                "roles", request.getRoles().stream().map(Enum::name).sorted().toList()));
        profileCache.evict(userId);
        return SetUserRolesServiceResponse.builder().profile(UserProfile.from(user)).build();
    }
}
