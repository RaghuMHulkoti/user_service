package com.learning.user_service.web.controller;

import com.learning.user_service.web.model.ChangePasswordWebRequest;
import com.learning.user_service.web.model.RecordConsentWebRequest;
import com.learning.user_service.web.model.UpdateProfileWebRequest;
import com.learning.user_service.web.response.ConsentResponse;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.core.processor.profile.get.GetProfileProcessor;
import com.learning.user_service.core.processor.profile.update.UpdateProfileProcessor;
import com.learning.user_service.core.processor.profile.delete.DeleteAccountProcessor;
import com.learning.user_service.core.processor.profile.changepassword.ChangePasswordProcessor;
import com.learning.user_service.core.processor.consent.current.GetCurrentConsentsProcessor;
import com.learning.user_service.core.processor.consent.history.GetConsentHistoryProcessor;
import com.learning.user_service.core.processor.consent.record.RecordConsentProcessor;
import com.learning.user_service.web.mapper.ProfileMapper;
import com.learning.user_service.web.mapper.PasswordMapper;
import com.learning.user_service.web.mapper.ConsentMapper;
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

    private final GetProfileProcessor getProfileProcessor;
    private final UpdateProfileProcessor updateProfileProcessor;
    private final DeleteAccountProcessor deleteAccountProcessor;
    private final ChangePasswordProcessor changePasswordProcessor;
    private final GetCurrentConsentsProcessor getCurrentConsentsProcessor;
    private final GetConsentHistoryProcessor getConsentHistoryProcessor;
    private final RecordConsentProcessor recordConsentProcessor;

    public MeController(GetProfileProcessor getProfileProcessor, UpdateProfileProcessor updateProfileProcessor,
                        DeleteAccountProcessor deleteAccountProcessor, ChangePasswordProcessor changePasswordProcessor,
                        GetCurrentConsentsProcessor getCurrentConsentsProcessor,
                        GetConsentHistoryProcessor getConsentHistoryProcessor,
                        RecordConsentProcessor recordConsentProcessor) {
        this.getProfileProcessor = getProfileProcessor;
        this.updateProfileProcessor = updateProfileProcessor;
        this.deleteAccountProcessor = deleteAccountProcessor;
        this.changePasswordProcessor = changePasswordProcessor;
        this.getCurrentConsentsProcessor = getCurrentConsentsProcessor;
        this.getConsentHistoryProcessor = getConsentHistoryProcessor;
        this.recordConsentProcessor = recordConsentProcessor;
    }

    @Operation(summary = "Profile")
    @GetMapping
    public UserProfile me(@AuthenticationPrincipal Jwt jwt) {
        UserProfile profile = ProfileMapper.mapGetProfileResponse(
                getProfileProcessor.process(ProfileMapper.toGetProfileRequest(CurrentUser.id(jwt))));
        if (profile.status() == UserStatus.SUSPENDED) {
            throw new ApiException(ErrorCode.ACCOUNT_SUSPENDED, "Account is suspended");
        }
        return profile;
    }

    @Operation(summary = "Update profile (partial). Send 'version' for optimistic locking.")
    @PatchMapping
    public UserProfile update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileWebRequest cmd) {
        return ProfileMapper.mapUpdateProfileResponse(
                updateProfileProcessor.process(ProfileMapper.toUpdateProfileRequest(CurrentUser.id(jwt), cmd)));
    }

    @Operation(summary = "Delete account (anonymises personal data)")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt) {
        deleteAccountProcessor.process(ProfileMapper.toDeleteAccountRequest(CurrentUser.id(jwt)));
    }

    @Operation(summary = "Change password (signs out all sessions)")
    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordWebRequest cmd) {
        changePasswordProcessor.process(PasswordMapper.toChangePasswordRequest(CurrentUser.id(jwt), cmd));
    }

    @Operation(summary = "Current consent decisions (latest per kind)")
    @GetMapping("/consents")
    public List<ConsentResponse> consents(@AuthenticationPrincipal Jwt jwt) {
        return ConsentMapper.mapGetCurrentConsentsResponse(
                getCurrentConsentsProcessor.process(ConsentMapper.toGetCurrentConsentsRequest(CurrentUser.id(jwt))));
    }

    @Operation(summary = "Full consent history")
    @GetMapping("/consents/history")
    public List<ConsentResponse> consentHistory(@AuthenticationPrincipal Jwt jwt) {
        return ConsentMapper.mapGetConsentHistoryResponse(
                getConsentHistoryProcessor.process(ConsentMapper.toGetConsentHistoryRequest(CurrentUser.id(jwt))));
    }

    @Operation(summary = "Grant or withdraw a consent")
    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsentResponse recordConsent(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RecordConsentWebRequest cmd,
                                     HttpServletRequest request) {
        return ConsentMapper.mapRecordConsentResponse(recordConsentProcessor.process(
                ConsentMapper.toRecordConsentRequest(CurrentUser.id(jwt), cmd, request.getRemoteAddr())));
    }
}
