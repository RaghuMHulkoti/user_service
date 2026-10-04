package com.learning.user_service.application.service;

import com.learning.user_service.application.command.LoginCommand;
import com.learning.user_service.application.command.LogoutCommand;
import com.learning.user_service.application.command.RefreshTokenCommand;
import com.learning.user_service.application.query.TokenPair;
import com.learning.user_service.application.support.SecureTokens;
import com.learning.user_service.domain.model.UserStatus;
import com.learning.user_service.domain.port.AccessTokenFactory;
import com.learning.user_service.domain.port.RateLimiter;
import com.learning.user_service.domain.port.RefreshTokenStore;
import com.learning.user_service.infrastructure.persistence.entity.PasswordCredentialEntity;
import com.learning.user_service.infrastructure.persistence.entity.UserEntity;
import com.learning.user_service.infrastructure.persistence.repository.PasswordCredentialRepository;
import com.learning.user_service.infrastructure.persistence.repository.UserRepository;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
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
class AuthServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");
    private static final String EMAIL = "raghu@example.com";
    private static final String PASSWORD = "Str0ng-Passw0rd!";

    @Mock UserRepository userRepository;
    @Mock PasswordCredentialRepository credentialRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AccessTokenFactory accessTokenFactory;
    @Mock RefreshTokenStore refreshTokenStore;
    @Mock RateLimiter rateLimiter;

    private AuthService authService;
    private UserEntity user;
    private PasswordCredentialEntity credential;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        authService = new AuthService(userRepository, credentialRepository, passwordEncoder, accessTokenFactory,
                refreshTokenStore, rateLimiter, TestProperties.defaults(), Clock.fixed(NOW, ZoneOffset.UTC));

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
        when(userRepository.findActiveByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(credentialRepository.findById(user.getId())).thenReturn(Optional.of(credential));
    }

    @Test
    void loginIssuesTokenPairAndResetsFailureCounter() {
        userExists();
        credential.setFailedAttempts(3);
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(true);

        TokenPair tokens = authService.login(new LoginCommand(" Raghu@Example.com ", PASSWORD), "10.0.0.1");

        assertThat(tokens.accessToken()).isEqualTo("jwt");
        assertThat(tokens.expiresIn()).isEqualTo(900);
        assertThat(tokens.refreshExpiresIn()).isEqualTo(Duration.ofDays(30).toSeconds());
        assertThat(tokens.refreshToken()).contains(".");
        assertThat(credential.getFailedAttempts()).isZero();
        verify(userRepository).recordLogin(user.getId(), NOW);
        String jti = tokens.refreshToken().substring(0, tokens.refreshToken().indexOf('.'));
        String secret = tokens.refreshToken().substring(tokens.refreshToken().indexOf('.') + 1);
        verify(refreshTokenStore).save(jti, user.getId(), SecureTokens.sha256Hex(secret), Duration.ofDays(30));
    }

    @Test
    void loginIsRejectedWhenIpLimitIsHit() {
        when(rateLimiter.tryAcquire(startsWith("login:ip:"), eq(5), eq(Duration.ofMinutes(1)))).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, PASSWORD), "10.0.0.1"))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.RATE_LIMITED);
        verify(userRepository, never()).findActiveByEmail(anyString());
    }

    @Test
    void unknownEmailStillRunsBcryptAndGivesGenericError() {
        when(userRepository.findActiveByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, PASSWORD), "10.0.0.1"))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(passwordEncoder).matches(PASSWORD, "dummy-hash");
    }

    @Test
    void wrongPasswordCountsFailure() {
        userExists();
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, PASSWORD), "10.0.0.1"))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        assertThat(credential.getFailedAttempts()).isEqualTo(1);
        assertThat(credential.getLockedUntil()).isNull();
    }

    @Test
    void fifthWrongPasswordLocksAccountFor15Minutes() {
        userExists();
        credential.setFailedAttempts(4);
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, PASSWORD), "10.0.0.1"))
                .isInstanceOf(ApiException.class);
        assertThat(credential.getLockedUntil()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
    }

    @Test
    void lockedAccountIsRejectedEvenWithCorrectPassword() {
        userExists();
        credential.setLockedUntil(NOW.plusSeconds(60));

        assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, PASSWORD), "10.0.0.1"))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.ACCOUNT_LOCKED);
        verify(passwordEncoder, never()).matches(PASSWORD, "real-hash");
    }

    @Test
    void suspendedAccountCannotLogIn() {
        userExists();
        user.setStatus(UserStatus.SUSPENDED);
        when(passwordEncoder.matches(PASSWORD, "real-hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, PASSWORD), "10.0.0.1"))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED);
    }

    @Test
    void refreshConsumesOldTokenAndIssuesNewPair() {
        String jti = UUID.randomUUID().toString();
        when(refreshTokenStore.consume(jti)).thenReturn(Optional.of(
                new RefreshTokenStore.StoredRefreshToken(jti, user.getId(), SecureTokens.sha256Hex("secret"))));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        TokenPair tokens = authService.refresh(new RefreshTokenCommand(jti + ".secret"));

        assertThat(tokens.refreshToken()).doesNotStartWith(jti);
        verify(refreshTokenStore).save(anyString(), eq(user.getId()), anyString(), eq(Duration.ofDays(30)));
    }

    @Test
    void refreshWithWrongSecretFails() {
        String jti = UUID.randomUUID().toString();
        when(refreshTokenStore.consume(jti)).thenReturn(Optional.of(
                new RefreshTokenStore.StoredRefreshToken(jti, user.getId(), SecureTokens.sha256Hex("secret"))));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenCommand(jti + ".guess")))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void refreshWithMalformedTokenFails() {
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenCommand("not-a-token")))
                .extracting(e -> ((ApiException) e).code()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void logoutDoesNotRevokeSomeoneElsesToken() {
        String jti = UUID.randomUUID().toString();
        when(refreshTokenStore.find(jti)).thenReturn(Optional.of(
                new RefreshTokenStore.StoredRefreshToken(jti, UUID.randomUUID(), "hash")));

        authService.logout(user.getId(), new LogoutCommand(jti + ".secret", false));

        verify(refreshTokenStore, never()).revoke(anyString());
    }

    @Test
    void logoutEverywhereRevokesAllSessions() {
        authService.logout(user.getId(), new LogoutCommand(null, true));

        verify(refreshTokenStore).revokeAllForUser(user.getId());
    }
}
