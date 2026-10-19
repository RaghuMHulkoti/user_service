package com.learning.user_service.web.mapper;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.processor.admin.list.ListUsersProcessorRequest;
import com.learning.user_service.core.processor.admin.list.ListUsersProcessorResponse;
import com.learning.user_service.core.processor.admin.reactivate.ReactivateUserProcessorRequest;
import com.learning.user_service.core.processor.admin.reactivate.ReactivateUserProcessorResponse;
import com.learning.user_service.core.processor.admin.setroles.SetUserRolesProcessorRequest;
import com.learning.user_service.core.processor.admin.setroles.SetUserRolesProcessorResponse;
import com.learning.user_service.core.processor.admin.suspend.SuspendUserProcessorRequest;
import com.learning.user_service.core.processor.admin.suspend.SuspendUserProcessorResponse;
import com.learning.user_service.web.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class AdminMapper {

    private AdminMapper() {
    }

    public static ListUsersProcessorRequest toListUsersRequest(UserStatus status, Pageable pageable) {
        return ListUsersProcessorRequest.builder().status(status).pageable(pageable).build();
    }

    public static PageResponse<UserProfile> mapListUsersResponse(ListUsersProcessorResponse response) {
        return PageResponse.from(response.getPage(), Function.identity());
    }

    public static SetUserRolesProcessorRequest toSetUserRolesRequest(UUID userId, Set<RoleCode> roles) {
        return SetUserRolesProcessorRequest.builder().userId(userId).roles(roles).build();
    }

    public static UserProfile mapSetUserRolesResponse(SetUserRolesProcessorResponse response) {
        return response.getProfile();
    }

    public static SuspendUserProcessorRequest toSuspendUserRequest(UUID userId) {
        return SuspendUserProcessorRequest.builder().userId(userId).build();
    }

    public static UserProfile mapSuspendUserResponse(SuspendUserProcessorResponse response) {
        return response.getProfile();
    }

    public static ReactivateUserProcessorRequest toReactivateUserRequest(UUID userId) {
        return ReactivateUserProcessorRequest.builder().userId(userId).build();
    }

    public static UserProfile mapReactivateUserResponse(ReactivateUserProcessorResponse response) {
        return response.getProfile();
    }
}
