package com.learning.user_service.presentation.rest;

import com.learning.user_service.application.command.LoginCommand;
import com.learning.user_service.application.command.LogoutCommand;
import com.learning.user_service.application.command.PasswordResetCommand;
import com.learning.user_service.application.command.PasswordResetRequestCommand;
import com.learning.user_service.application.command.RefreshTokenCommand;
import com.learning.user_service.application.command.RegisterUserCommand;
import com.learning.user_service.application.command.VerifyEmailCommand;
import com.learning.user_service.application.query.RegistrationResult;
import com.learning.user_service.application.query.TokenPair;
import com.learning.user_service.application.service.AuthService;
import com.learning.user_service.application.service.PasswordService;
import com.learning.user_service.application.service.RegistrationService;
import com.learning.user_service.application.support.IdempotencyService;
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

    private final RegistrationService registrationService;
    private final AuthService authService;
    private final PasswordService passwordService;
    private final IdempotencyService idempotencyService;

    public AuthController(RegistrationService registrationService, AuthService authService,
                          PasswordService passwordService, IdempotencyService idempotencyService) {
        this.registrationService = registrationService;
        this.authService = authService;
        this.passwordService = passwordService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "Create account")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResult> register(
            @RequestHeader(name = IdempotentResponses.KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody RegisterUserCommand cmd) {
        // The stored request hash must not be derived from the password (it would allow offline guessing).
        RegisterUserCommand fingerprint = new RegisterUserCommand(cmd.email(), null, cmd.firstName(),
                cmd.lastName(), cmd.phone(), cmd.locale());
        return IdempotentResponses.created(idempotencyService.execute("register", idempotencyKey, fingerprint,
                RegistrationResult.class, () -> registrationService.register(cmd)));
    }

    @Operation(summary = "Confirm email with the token from the verification email")
    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailCommand cmd) {
        registrationService.verifyEmail(cmd);
    }

    @Operation(summary = "Send a new verification email")
    @PostMapping("/verify-email/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendVerification(@AuthenticationPrincipal Jwt jwt) {
        registrationService.resendVerification(CurrentUser.id(jwt));
    }

    @Operation(summary = "Issue access + refresh tokens")
    @PostMapping("/login")
    public TokenPair login(@Valid @RequestBody LoginCommand cmd, HttpServletRequest request) {
        return authService.login(cmd, request.getRemoteAddr());
    }

    @Operation(summary = "Swap a refresh token for a new token pair (rotation)")
    @PostMapping("/refresh")
    public TokenPair refresh(@Valid @RequestBody RefreshTokenCommand cmd) {
        return authService.refresh(cmd);
    }

    @Operation(summary = "Revoke a refresh token, or all sessions with allSessions=true")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody LogoutCommand cmd) {
        authService.logout(CurrentUser.id(jwt), cmd);
    }

    @Operation(summary = "Send a password reset email (always 202, even for unknown emails)")
    @PostMapping("/password/reset-request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequestCommand cmd) {
        passwordService.requestReset(cmd);
    }

    @Operation(summary = "Set a new password with the one-time reset token")
    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetCommand cmd) {
        passwordService.reset(cmd);
    }
}
