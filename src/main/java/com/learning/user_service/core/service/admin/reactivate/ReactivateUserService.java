package com.learning.user_service.core.service.admin.reactivate;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.AdminUserSupport;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Operator tools: reactivate a suspended account. */
@Component
public class ReactivateUserService implements Service<ReactivateUserServiceRequest, ReactivateUserServiceResponse> {

    private final AdminUserSupport adminUserSupport;

    public ReactivateUserService(AdminUserSupport adminUserSupport) {
        this.adminUserSupport = adminUserSupport;
    }

    @Override
    @Transactional
    public ReactivateUserServiceResponse execute(ReactivateUserServiceRequest request) {
        UserEntity user = adminUserSupport.findManageable(request.getUserId());
        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_VIOLATION, "Only suspended accounts can be reactivated");
        }
        user.setStatus(user.getEmailVerifiedAt() == null ? UserStatus.PENDING_VERIFICATION : UserStatus.ACTIVE);
        adminUserSupport.statusChanged(user, false);
        return ReactivateUserServiceResponse.builder().profile(UserProfile.from(user)).build();
    }
}
