package com.learning.user_service.core.service.auth.refresh;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.SecureTokens;
import com.learning.user_service.core.support.SessionSupport;
import com.learning.user_service.core.support.SessionSupport.ParsedRefreshToken;
import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Swaps a refresh token for a new token pair (rotation, TDD §12.2). */
@Component
public class RefreshTokenService implements Service<RefreshTokenServiceRequest, RefreshTokenServiceResponse> {

    private final UserGateway userGateway;
    private final RefreshTokenStore refreshTokenStore;
    private final SessionSupport sessionSupport;

    public RefreshTokenService(UserGateway userGateway, RefreshTokenStore refreshTokenStore,
                               SessionSupport sessionSupport) {
        this.userGateway = userGateway;
        this.refreshTokenStore = refreshTokenStore;
        this.sessionSupport = sessionSupport;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenServiceResponse execute(RefreshTokenServiceRequest request) {
        ParsedRefreshToken parsed = ParsedRefreshToken.parse(request.getRefreshToken())
                .orElseThrow(SessionSupport::invalidRefreshToken);
        // Rotation: consume() deletes the old token atomically, so it works exactly once.
        RefreshTokenStore.StoredRefreshToken stored = refreshTokenStore.consume(parsed.jti())
                .filter(t -> SecureTokens.constantTimeEquals(t.secretHash(), SecureTokens.sha256Hex(parsed.secret())))
                .orElseThrow(SessionSupport::invalidRefreshToken);

        UserEntity user = userGateway.findById(stored.userId())
                .filter(u -> u.getStatus().canLogIn())
                .orElseThrow(SessionSupport::invalidRefreshToken);
        return RefreshTokenServiceResponse.builder().tokenPair(sessionSupport.issueTokens(user)).build();
    }
}
