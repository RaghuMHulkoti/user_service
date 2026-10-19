package com.learning.user_service.core.service.admin.suspend;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.AdminUserSupport;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Operator tools: suspend an account and sign the user out everywhere. */
@Component
public class SuspendUserService implements Service<SuspendUserServiceRequest, SuspendUserServiceResponse> {

    private final AdminUserSupport adminUserSupport;

    public SuspendUserService(AdminUserSupport adminUserSupport) {
        this.adminUserSupport = adminUserSupport;
    }

    @Override
    @Transactional
    public SuspendUserServiceResponse execute(SuspendUserServiceRequest request) {
        UserEntity user = adminUserSupport.findManageable(request.getUserId());
        if (user.getStatus() == UserStatus.SUSPENDED) {
            return SuspendUserServiceResponse.builder().profile(UserProfile.from(user)).build();
        }
        user.setStatus(UserStatus.SUSPENDED);
        adminUserSupport.statusChanged(user, true);
        return SuspendUserServiceResponse.builder().profile(UserProfile.from(user)).build();
    }
}
