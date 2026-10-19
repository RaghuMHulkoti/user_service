package com.learning.user_service.core.service;

import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Loads the calling user and rejects deleted or suspended accounts. */
@Component
public class UserAccess {

    private final UserGateway userGateway;

    public UserAccess(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public UserEntity loadUsable(UUID userId) {
        UserEntity user = userGateway.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .orElseThrow(() -> ApiException.notFound("User"));
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "Account is suspended");
        }
        return user;
    }
}
