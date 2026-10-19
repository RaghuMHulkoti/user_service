package com.learning.user_service.web.mapper;

import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.core.processor.auth.login.LoginProcessorRequest;
import com.learning.user_service.core.processor.auth.login.LoginProcessorResponse;
import com.learning.user_service.core.processor.auth.logout.LogoutProcessorRequest;
import com.learning.user_service.core.processor.auth.refresh.RefreshTokenProcessorRequest;
import com.learning.user_service.core.processor.auth.refresh.RefreshTokenProcessorResponse;
import com.learning.user_service.web.model.LoginWebRequest;
import com.learning.user_service.web.model.LogoutWebRequest;
import com.learning.user_service.web.model.RefreshTokenWebRequest;

import java.util.UUID;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static LoginProcessorRequest toLoginRequest(LoginWebRequest request, String clientIp) {
        return LoginProcessorRequest.builder()
                .email(request.email())
                .password(request.password())
                .clientIp(clientIp)
                .build();
    }

    public static TokenPair mapLoginResponse(LoginProcessorResponse response) {
        return response.getTokenPair();
    }

    public static RefreshTokenProcessorRequest toRefreshTokenRequest(RefreshTokenWebRequest request) {
        return RefreshTokenProcessorRequest.builder()
                .refreshToken(request.refreshToken())
                .build();
    }

    public static TokenPair mapRefreshTokenResponse(RefreshTokenProcessorResponse response) {
        return response.getTokenPair();
    }

    public static LogoutProcessorRequest toLogoutRequest(UUID userId, LogoutWebRequest request) {
        return LogoutProcessorRequest.builder()
                .userId(userId)
                .refreshToken(request.refreshToken())
                .allSessions(request.allSessions())
                .build();
    }
}
