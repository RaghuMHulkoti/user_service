package com.learning.user_service.core.service.auth;

import com.learning.user_service.core.service.auth.logout.LogoutService;
import com.learning.user_service.core.service.auth.logout.LogoutServiceRequest;
import com.learning.user_service.gateway.RefreshTokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock RefreshTokenStore refreshTokenStore;

    private LogoutService logoutService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(refreshTokenStore);
        userId = UUID.randomUUID();
    }

    @Test
    void logoutDoesNotRevokeSomeoneElsesToken() {
        String jti = UUID.randomUUID().toString();
        when(refreshTokenStore.find(jti)).thenReturn(Optional.of(
                new RefreshTokenStore.StoredRefreshToken(jti, UUID.randomUUID(), "hash")));

        logoutService.execute(LogoutServiceRequest.builder()
                .userId(userId).refreshToken(jti + ".secret").allSessions(false).build());

        verify(refreshTokenStore, never()).revoke(anyString());
    }

    @Test
    void logoutEverywhereRevokesAllSessions() {
        logoutService.execute(LogoutServiceRequest.builder()
                .userId(userId).refreshToken(null).allSessions(true).build());

        verify(refreshTokenStore).revokeAllForUser(userId);
    }
}
