package com.learning.user_service.core.service.auth;

import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.service.auth.refresh.RefreshTokenService;
import com.learning.user_service.core.service.auth.refresh.RefreshTokenServiceRequest;
import com.learning.user_service.core.support.SecureTokens;
import com.learning.user_service.core.support.SessionSupport;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.AccessTokenFactory;
import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.support.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final String EMAIL = "raghu@example.com";

    @Mock UserGateway userGateway;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AccessTokenFactory accessTokenFactory;
    @Mock RefreshTokenStore refreshTokenStore;

    private RefreshTokenService refreshTokenService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        SessionSupport sessionSupport = new SessionSupport(passwordEncoder, accessTokenFactory, refreshTokenStore,
                TestProperties.defaults());
        refreshTokenService = new RefreshTokenService(userGateway, refreshTokenStore, sessionSupport);

        user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(EMAIL);
        user.setStatus(UserStatus.ACTIVE);
        lenient().when(accessTokenFactory.issue(any(), any()))
                .thenReturn(new AccessTokenFactory.IssuedAccessToken("jwt", Duration.ofMinutes(15)));
    }

    private static RefreshTokenServiceRequest refresh(String token) {
        return RefreshTokenServiceRequest.builder().refreshToken(token).build();
    }

    @Test
    void refreshConsumesOldTokenAndIssuesNewPair() {
        String jti = UUID.randomUUID().toString();
        when(refreshTokenStore.consume(jti)).thenReturn(Optional.of(
                new RefreshTokenStore.StoredRefreshToken(jti, user.getId(), SecureTokens.sha256Hex("secret"))));
        when(userGateway.findById(user.getId())).thenReturn(Optional.of(user));

        TokenPair tokens = refreshTokenService.execute(refresh(jti + ".secret")).getTokenPair();

        assertThat(tokens.refreshToken()).doesNotStartWith(jti);
        verify(refreshTokenStore).save(anyString(), eq(user.getId()), anyString(), eq(Duration.ofDays(30)));
    }

    @Test
    void refreshWithWrongSecretFails() {
        String jti = UUID.randomUUID().toString();
        when(refreshTokenStore.consume(jti)).thenReturn(Optional.of(
                new RefreshTokenStore.StoredRefreshToken(jti, user.getId(), SecureTokens.sha256Hex("secret"))));

        assertThatThrownBy(() -> refreshTokenService.execute(refresh(jti + ".guess")))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void refreshWithMalformedTokenFails() {
        assertThatThrownBy(() -> refreshTokenService.execute(refresh("not-a-token")))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }
}
