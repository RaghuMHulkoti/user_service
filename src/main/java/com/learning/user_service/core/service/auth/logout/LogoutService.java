package com.learning.user_service.core.service.auth.logout;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.SessionSupport.ParsedRefreshToken;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.RefreshTokenStore;
import org.springframework.stereotype.Component;

/** Revokes one refresh token, or every session of the user when allSessions is true. */
@Component
public class LogoutService implements Service<LogoutServiceRequest, LogoutServiceResponse> {

    private final RefreshTokenStore refreshTokenStore;

    public LogoutService(RefreshTokenStore refreshTokenStore) {
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public LogoutServiceResponse execute(LogoutServiceRequest request) {
        if (Boolean.TRUE.equals(request.getAllSessions())) {
            refreshTokenStore.revokeAllForUser(request.getUserId());
            return LogoutServiceResponse.builder().success(true).build();
        }
        if (request.getRefreshToken() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "refreshToken is required unless allSessions is true");
        }
        ParsedRefreshToken.parse(request.getRefreshToken())
                .flatMap(parsed -> refreshTokenStore.find(parsed.jti()))
                .filter(stored -> stored.userId().equals(request.getUserId()))
                .ifPresent(stored -> refreshTokenStore.revoke(stored.jti()));
        return LogoutServiceResponse.builder().success(true).build();
    }
}
