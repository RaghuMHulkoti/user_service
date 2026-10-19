package com.learning.user_service.core.service.auth;

import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.service.auth.login.LoginService;
import com.learning.user_service.core.service.auth.login.LoginServiceRequest;
import com.learning.user_service.core.support.SecureTokens;
import com.learning.user_service.core.support.SessionSupport;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.AccessTokenFactory;
import com.learning.user_service.gateway.PasswordCredentialGateway;
import com.learning.user_service.gateway.RateLimiter;
import com.learning.user_service.gateway.RefreshTokenStore;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import com.learning.user_service.support.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");
    private static final String EMAIL = "raghu@example.com";
    private static final String PASSWORD = "Str0ng-Passw0rd!";

    @Mock UserGateway userGateway;
    @Mock PasswordCredentialGateway credentialGateway;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AccessTokenFactory accessTokenFactory;
    @Mock RefreshTokenStore refreshTokenStore;
    @Mock RateLimiter rateLimiter;

    private LoginService loginService;
    private UserEntity user;
    private PasswordCredentialEntity credential;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        SessionSupport sessionSupport = new SessionSupport(passwordEncoder, accessTokenFactory, refreshTokenStore,
                TestProperties.defaults());
        loginService = new LoginService(userGateway, credentialGateway, passwordEncoder, rateLimiter, sessionSupport,
                TestProperties.defaults(), Clock.fixed(NOW, ZoneOffset.UTC));

        user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(EMAIL);
        user.setStatus(UserStatus.ACTIVE);
        credential = new PasswordCredentialEntity(user.getId(), "real-hash");
        lenient().when(rateLimiter.tryAcquire(anyString(), anyInt(), any())).thenReturn(true);
        lenient().when(accessTokenFactory.issue(any(), any()))
                .thenReturn(new AccessTokenFactory.IssuedAccessToken("jwt", Duration.ofMinutes(15)));
    }

    private void userExists() {
        when(userGateway.findActiveByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(credentialGateway.findById(user.getId())).thenReturn(Optional.of(credential));
    }

    private LoginServiceRequest login(String email) {
        return LoginServiceRequest.builder().email(email).password(PASSWORD).clientIp("10.0.0.1").build();
    }

    @Test
    void loginIssuesTokenPairAndResetsFailureCounter() {
        userExists();
        credential.setFailedAttempts(3);
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(true);

        TokenPair tokens = loginService.execute(login(" Raghu@Example.com ")).getTokenPair();

        assertThat(tokens.accessToken()).isEqualTo("jwt");
        assertThat(tokens.expiresIn()).isEqualTo(900);
        assertThat(tokens.refreshExpiresIn()).isEqualTo(Duration.ofDays(30).toSeconds());
        assertThat(tokens.refreshToken()).contains(".");
        assertThat(credential.getFailedAttempts()).isZero();
        verify(userGateway).recordLogin(user.getId(), NOW);
        String jti = tokens.refreshToken().substring(0, tokens.refreshToken().indexOf('.'));
        String secret = tokens.refreshToken().substring(tokens.refreshToken().indexOf('.') + 1);
        verify(refreshTokenStore).save(jti, user.getId(), SecureTokens.sha256Hex(secret), Duration.ofDays(30));
    }

    @Test
    void loginIsRejectedWhenIpLimitIsHit() {
        when(rateLimiter.tryAcquire(startsWith("login:ip:"), eq(5), eq(Duration.ofMinutes(1)))).thenReturn(false);

        assertThatThrownBy(() -> loginService.execute(login(EMAIL)))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.RATE_LIMITED);
        verify(userGateway, never()).findActiveByEmail(anyString());
    }

    @Test
    void unknownEmailStillRunsBcryptAndGivesGenericError() {
        when(userGateway.findActiveByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.execute(login(EMAIL)))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(passwordEncoder).matches(PASSWORD, "dummy-hash");
    }

    @Test
    void wrongPasswordCountsFailure() {
        userExists();
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> loginService.execute(login(EMAIL)))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        assertThat(credential.getFailedAttempts()).isEqualTo(1);
        assertThat(credential.getLockedUntil()).isNull();
    }

    @Test
    void fifthWrongPasswordLocksAccountFor15Minutes() {
        userExists();
        credential.setFailedAttempts(4);
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> loginService.execute(login(EMAIL)))
                .isInstanceOf(ApiException.class);
        assertThat(credential.getLockedUntil()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
    }

    @Test
    void lockedAccountIsRejectedEvenWithCorrectPassword() {
        userExists();
        credential.setLockedUntil(NOW.plusSeconds(60));

        assertThatThrownBy(() -> loginService.execute(login(EMAIL)))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.ACCOUNT_LOCKED);
        verify(passwordEncoder, never()).matches(PASSWORD, "real-hash");
    }

    @Test
    void suspendedAccountCannotLogIn() {
        userExists();
        user.setStatus(UserStatus.SUSPENDED);
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(true);

        assertThatThrownBy(() -> loginService.execute(login(EMAIL)))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED);
    }
}
