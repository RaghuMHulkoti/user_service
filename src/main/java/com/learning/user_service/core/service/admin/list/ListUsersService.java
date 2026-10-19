package com.learning.user_service.core.service.admin.list;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Operator tools: list users. */
@Component
public class ListUsersService implements Service<ListUsersServiceRequest, ListUsersServiceResponse> {

    private final UserGateway userGateway;

    public ListUsersService(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    @Override
    @Transactional(readOnly = true)
    public ListUsersServiceResponse execute(ListUsersServiceRequest request) {
        Page<UserEntity> page = request.getStatus() == null
                ? userGateway.findAll(request.getPageable())
                : userGateway.findByStatus(request.getStatus(), request.getPageable());
        return ListUsersServiceResponse.builder().page(page.map(UserProfile::from)).build();
    }
}
