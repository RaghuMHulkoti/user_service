package com.learning.user_service.application.service;

import com.learning.user_service.domain.model.UserStatus;
import com.learning.user_service.infrastructure.persistence.entity.UserEntity;
import com.learning.user_service.infrastructure.persistence.repository.UserRepository;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Loads the calling user and rejects deleted or suspended accounts. */
@Component
public class UserAccess {

    private final UserRepository userRepository;

    public UserAccess(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity loadUsable(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .orElseThrow(() -> ApiException.notFound("User"));
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "Account is suspended");
        }
        return user;
    }
}
