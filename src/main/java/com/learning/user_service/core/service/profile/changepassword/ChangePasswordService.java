package com.learning.user_service.core.service.profile.changepassword;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.PasswordChangeSupport;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.PasswordCredentialGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChangePasswordService implements Service<ChangePasswordServiceRequest, ChangePasswordServiceResponse> {

    private final PasswordCredentialGateway credentialGateway;
    private final PasswordEncoder passwordEncoder;
    private final PasswordChangeSupport passwordChangeSupport;
    private final UserAccess userAccess;

    public ChangePasswordService(PasswordCredentialGateway credentialGateway, PasswordEncoder passwordEncoder,
                                 PasswordChangeSupport passwordChangeSupport, UserAccess userAccess) {
        this.credentialGateway = credentialGateway;
        this.passwordEncoder = passwordEncoder;
        this.passwordChangeSupport = passwordChangeSupport;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional
    public ChangePasswordServiceResponse execute(ChangePasswordServiceRequest request) {
        UserEntity user = userAccess.loadUsable(request.getUserId());
        PasswordCredentialEntity cred = credentialGateway.findById(user.getId())
                .orElseThrow(() -> ApiException.notFound("Credentials"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), cred.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect");
        }
        PasswordChangeSupport.requireStrong(request.getNewPassword(), "newPassword");
        cred.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        passwordChangeSupport.passwordChanged(user.getId(), "CHANGE");
        return ChangePasswordServiceResponse.builder().success(true).build();
    }
}
