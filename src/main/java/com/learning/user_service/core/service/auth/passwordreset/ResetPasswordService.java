package com.learning.user_service.core.service.auth.passwordreset;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.PasswordChangeSupport;
import com.learning.user_service.core.support.SecureTokens;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.PasswordCredentialGateway;
import com.learning.user_service.gateway.PasswordResetTokenGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordResetTokenEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
public class ResetPasswordService implements Service<ResetPasswordServiceRequest, ResetPasswordServiceResponse> {

    private final PasswordCredentialGateway credentialGateway;
    private final PasswordResetTokenGateway resetTokenGateway;
    private final PasswordEncoder passwordEncoder;
    private final PasswordChangeSupport passwordChangeSupport;
    private final UserAccess userAccess;
    private final Clock clock;

    public ResetPasswordService(PasswordCredentialGateway credentialGateway,
                                PasswordResetTokenGateway resetTokenGateway, PasswordEncoder passwordEncoder,
                                PasswordChangeSupport passwordChangeSupport, UserAccess userAccess, Clock clock) {
        this.credentialGateway = credentialGateway;
        this.resetTokenGateway = resetTokenGateway;
        this.passwordEncoder = passwordEncoder;
        this.passwordChangeSupport = passwordChangeSupport;
        this.userAccess = userAccess;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ResetPasswordServiceResponse execute(ResetPasswordServiceRequest request) {
        PasswordChangeSupport.requireStrong(request.getNewPassword(), "newPassword");
        Instant now = clock.instant();
        PasswordResetTokenEntity token = resetTokenGateway.findByTokenHash(SecureTokens.sha256Hex(request.getToken()))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Reset link is invalid or has expired"));
        token.setConsumedAt(now);

        UserEntity user = userAccess.loadUsable(token.getUserId());
        PasswordCredentialEntity cred = credentialGateway.findById(user.getId())
                .orElseGet(() -> new PasswordCredentialEntity(user.getId(), ""));
        cred.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        cred.setFailedAttempts(0);
        cred.setLockedUntil(null);
        credentialGateway.save(cred);

        // Clicking the emailed link proves the user owns the address.
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(now);
        }
        passwordChangeSupport.passwordChanged(user.getId(), "RESET");
        return ResetPasswordServiceResponse.builder().success(true).build();
    }
}
