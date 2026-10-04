package com.learning.user_service.presentation.rest;

import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

final class CurrentUser {

    private CurrentUser() {
    }

    static UUID id(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Token subject is not a user id");
        }
    }
}
