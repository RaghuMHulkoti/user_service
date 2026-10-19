package com.learning.user_service.web.controller;

import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
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
