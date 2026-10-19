package com.learning.user_service.core.service.auth.register;

import com.learning.user_service.core.domain.RegistrationResult;
import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.domain.model.RoleCode;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.core.domain.policy.PasswordPolicy;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.support.EmailNormalizer;
import com.learning.user_service.core.support.VerificationTokenIssuer;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.PasswordCredentialGateway;
import com.learning.user_service.gateway.RoleGateway;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.credential.entity.PasswordCredentialEntity;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Account creation (TDD §8.2, §12.1). */
@Component
public class RegisterUserService implements Service<RegisterUserServiceRequest, RegisterUserServiceResponse> {

    private static final String DEFAULT_LOCALE = "en-IN";

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final PasswordCredentialGateway credentialGateway;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventRecorder events;
    private final VerificationTokenIssuer verificationTokenIssuer;

    public RegisterUserService(UserGateway userGateway, RoleGateway roleGateway,
                               PasswordCredentialGateway credentialGateway, PasswordEncoder passwordEncoder,
                               DomainEventRecorder events, VerificationTokenIssuer verificationTokenIssuer) {
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.credentialGateway = credentialGateway;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
        this.verificationTokenIssuer = verificationTokenIssuer;
    }

    /** One transaction: user + credentials + role + verification token + outbox events. */
    @Override
    @Transactional
    public RegisterUserServiceResponse execute(RegisterUserServiceRequest request) {
        List<String> passwordProblems = PasswordPolicy.violations(request.getPassword());
        if (!passwordProblems.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Request validation failed",
                    Map.of("fields", Map.of("password", String.join("; ", passwordProblems))));
        }
        String email = EmailNormalizer.normalize(request.getEmail());
        if (userGateway.existsActiveByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_REGISTERED, "An account with this email already exists");
        }
        if (request.getPhone() != null && userGateway.existsActiveByPhone(request.getPhone())) {
            throw new ApiException(ErrorCode.PHONE_ALREADY_REGISTERED, "An account with this phone already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPhone(request.getPhone());
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName() == null ? null : request.getLastName().trim());
        user.setLocale(request.getLocale() == null ? DEFAULT_LOCALE : request.getLocale());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.getRoles().add(roleGateway.findByCode(RoleCode.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role missing")));
        user = userGateway.saveAndFlush(user);

        credentialGateway.save(new PasswordCredentialEntity(user.getId(), passwordEncoder.encode(request.getPassword())));

        Map<String, Object> registered = new HashMap<>();
        registered.put("userId", user.getId().toString());
        registered.put("email", user.getEmail());
        registered.put("firstName", user.getFirstName());
        registered.put("lastName", user.getLastName());
        registered.put("phone", user.getPhone());
        registered.put("locale", user.getLocale());
        registered.put("roles", List.of(RoleCode.CUSTOMER.name()));
        events.record(user.getId(), UserEventType.USER_REGISTERED, registered);

        verificationTokenIssuer.issue(user);
        return RegisterUserServiceResponse.builder()
                .registrationResult(new RegistrationResult(user.getId(), user.getEmail(), user.getStatus(), true))
                .build();
    }
}
