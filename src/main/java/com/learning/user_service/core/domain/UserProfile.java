package com.learning.user_service.core.domain;

import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Profile view. Also the value cached under {@code user:{id}:profile}. */
public record UserProfile(
        UUID id,
        String email,
        String phone,
        String firstName,
        String lastName,
        UserStatus status,
        String locale,
        Set<RoleCode> roles,
        boolean emailVerified,
        boolean phoneVerified,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static UserProfile from(UserEntity user) {
        return new UserProfile(user.getId(), user.getEmail(), user.getPhone(), user.getFirstName(),
                user.getLastName(), user.getStatus(), user.getLocale(), user.roleCodes(),
                user.getEmailVerifiedAt() != null, user.getPhoneVerifiedAt() != null,
                user.getLastLoginAt(), user.getCreatedAt(), user.getUpdatedAt(), user.getVersion());
    }
}
