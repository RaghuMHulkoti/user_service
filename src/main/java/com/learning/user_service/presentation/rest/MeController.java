package com.learning.user_service.presentation.rest;

import com.learning.user_service.application.command.ChangePasswordCommand;
import com.learning.user_service.application.command.RecordConsentCommand;
import com.learning.user_service.application.command.UpdateProfileCommand;
import com.learning.user_service.application.query.ConsentView;
import com.learning.user_service.application.service.ConsentService;
import com.learning.user_service.application.service.PasswordService;
import com.learning.user_service.application.service.UserProfile;
import com.learning.user_service.application.service.UserProfileQueryService;
import com.learning.user_service.application.service.UserProfileService;
import com.learning.user_service.domain.model.UserStatus;
import com.learning.user_service.presentation.error.ApiException;
import com.learning.user_service.presentation.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "Profile", description = "The signed-in user's profile, password, consent and account")
public class MeController {

    private final UserProfileQueryService profileQueryService;
    private final UserProfileService profileService;
    private final PasswordService passwordService;
    private final ConsentService consentService;

    public MeController(UserProfileQueryService profileQueryService, UserProfileService profileService,
                        PasswordService passwordService, ConsentService consentService) {
        this.profileQueryService = profileQueryService;
        this.profileService = profileService;
        this.passwordService = passwordService;
        this.consentService = consentService;
    }

    @Operation(summary = "Profile")
    @GetMapping
    public UserProfile me(@AuthenticationPrincipal Jwt jwt) {
        UserProfile profile = profileQueryService.getProfile(CurrentUser.id(jwt));
        if (profile.status() == UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "Account is suspended");
        }
        return profile;
    }

    @Operation(summary = "Update profile (partial). Send 'version' for optimistic locking.")
    @PatchMapping
    public UserProfile update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileCommand cmd) {
        return profileService.update(CurrentUser.id(jwt), cmd);
    }

    @Operation(summary = "Delete account (anonymises personal data)")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt) {
        profileService.deleteAccount(CurrentUser.id(jwt));
    }

    @Operation(summary = "Change password (signs out all sessions)")
    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordCommand cmd) {
        passwordService.change(CurrentUser.id(jwt), cmd);
    }

    @Operation(summary = "Current consent decisions (latest per kind)")
    @GetMapping("/consents")
    public List<ConsentView> consents(@AuthenticationPrincipal Jwt jwt) {
        return consentService.current(CurrentUser.id(jwt));
    }

    @Operation(summary = "Full consent history")
    @GetMapping("/consents/history")
    public List<ConsentView> consentHistory(@AuthenticationPrincipal Jwt jwt) {
        return consentService.history(CurrentUser.id(jwt));
    }

    @Operation(summary = "Grant or withdraw a consent")
    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsentView recordConsent(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RecordConsentCommand cmd,
                                     HttpServletRequest request) {
        return consentService.record(CurrentUser.id(jwt), cmd, request.getRemoteAddr());
    }
}
