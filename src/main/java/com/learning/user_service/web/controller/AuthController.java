package com.learning.user_service.web.controller;

import com.learning.user_service.web.model.LoginWebRequest;
import com.learning.user_service.web.model.LogoutWebRequest;
import com.learning.user_service.web.model.PasswordResetWebRequest;
import com.learning.user_service.web.model.PasswordResetRequestWebRequest;
import com.learning.user_service.web.model.RefreshTokenWebRequest;
import com.learning.user_service.web.model.RegisterUserWebRequest;
import com.learning.user_service.web.model.VerifyEmailWebRequest;
import com.learning.user_service.core.domain.RegistrationResult;
import com.learning.user_service.core.domain.TokenPair;
import com.learning.user_service.core.support.IdempotencyService;
import com.learning.user_service.core.processor.auth.register.RegisterUserProcessor;
import com.learning.user_service.core.processor.auth.verifyemail.VerifyEmailProcessor;
import com.learning.user_service.core.processor.auth.resendverification.ResendVerificationProcessor;
import com.learning.user_service.core.processor.auth.login.LoginProcessor;
import com.learning.user_service.core.processor.auth.refresh.RefreshTokenProcessor;
import com.learning.user_service.core.processor.auth.logout.LogoutProcessor;
import com.learning.user_service.core.processor.auth.passwordresetrequest.RequestPasswordResetProcessor;
import com.learning.user_service.core.processor.auth.passwordreset.ResetPasswordProcessor;
import com.learning.user_service.web.mapper.PasswordMapper;
import com.learning.user_service.web.mapper.RegistrationMapper;
import com.learning.user_service.web.mapper.SessionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Auth", description = "Register, verify, log in, refresh, log out, reset password")
public class AuthController {

    private final RegisterUserProcessor registerUserProcessor;
    private final VerifyEmailProcessor verifyEmailProcessor;
    private final ResendVerificationProcessor resendVerificationProcessor;
    private final LoginProcessor loginProcessor;
    private final RefreshTokenProcessor refreshTokenProcessor;
    private final LogoutProcessor logoutProcessor;
    private final RequestPasswordResetProcessor requestPasswordResetProcessor;
    private final ResetPasswordProcessor resetPasswordProcessor;
    private final IdempotencyService idempotencyService;

    public AuthController(RegisterUserProcessor registerUserProcessor, VerifyEmailProcessor verifyEmailProcessor,
                          ResendVerificationProcessor resendVerificationProcessor, LoginProcessor loginProcessor,
                          RefreshTokenProcessor refreshTokenProcessor, LogoutProcessor logoutProcessor,
                          RequestPasswordResetProcessor requestPasswordResetProcessor,
                          ResetPasswordProcessor resetPasswordProcessor, IdempotencyService idempotencyService) {
        this.registerUserProcessor = registerUserProcessor;
        this.verifyEmailProcessor = verifyEmailProcessor;
        this.resendVerificationProcessor = resendVerificationProcessor;
        this.loginProcessor = loginProcessor;
        this.refreshTokenProcessor = refreshTokenProcessor;
        this.logoutProcessor = logoutProcessor;
        this.requestPasswordResetProcessor = requestPasswordResetProcessor;
        this.resetPasswordProcessor = resetPasswordProcessor;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "Create account")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResult> register(
            @RequestHeader(name = IdempotentResponses.KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody RegisterUserWebRequest cmd) {
        // The stored request hash must not be derived from the password (it would allow offline guessing).
        RegisterUserWebRequest fingerprint = new RegisterUserWebRequest(cmd.email(), null, cmd.firstName(),
                cmd.lastName(), cmd.phone(), cmd.locale());
        return IdempotentResponses.created(idempotencyService.execute("register", idempotencyKey, fingerprint,
                RegistrationResult.class, () -> RegistrationMapper.mapRegisterUserResponse(
                        registerUserProcessor.process(RegistrationMapper.toRegisterUserRequest(cmd)))));
    }

    @Operation(summary = "Confirm email with the token from the verification email")
    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailWebRequest cmd) {
        verifyEmailProcessor.process(RegistrationMapper.toVerifyEmailRequest(cmd));
    }

    @Operation(summary = "Send a new verification email")
    @PostMapping("/verify-email/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendVerification(@AuthenticationPrincipal Jwt jwt) {
        resendVerificationProcessor.process(RegistrationMapper.toResendVerificationRequest(CurrentUser.id(jwt)));
    }

    @Operation(summary = "Issue access + refresh tokens")
    @PostMapping("/login")
    public TokenPair login(@Valid @RequestBody LoginWebRequest cmd, HttpServletRequest request) {
        return SessionMapper.mapLoginResponse(
                loginProcessor.process(SessionMapper.toLoginRequest(cmd, request.getRemoteAddr())));
    }

    @Operation(summary = "Swap a refresh token for a new token pair (rotation)")
    @PostMapping("/refresh")
    public TokenPair refresh(@Valid @RequestBody RefreshTokenWebRequest cmd) {
        return SessionMapper.mapRefreshTokenResponse(
                refreshTokenProcessor.process(SessionMapper.toRefreshTokenRequest(cmd)));
    }

    @Operation(summary = "Revoke a refresh token, or all sessions with allSessions=true")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody LogoutWebRequest cmd) {
        logoutProcessor.process(SessionMapper.toLogoutRequest(CurrentUser.id(jwt), cmd));
    }

    @Operation(summary = "Send a password reset email (always 202, even for unknown emails)")
    @PostMapping("/password/reset-request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequestWebRequest cmd) {
        requestPasswordResetProcessor.process(PasswordMapper.toRequestPasswordResetRequest(cmd));
    }

    @Operation(summary = "Set a new password with the one-time reset token")
    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetWebRequest cmd) {
        resetPasswordProcessor.process(PasswordMapper.toResetPasswordRequest(cmd));
    }
}
