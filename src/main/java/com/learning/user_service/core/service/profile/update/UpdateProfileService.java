package com.learning.user_service.core.service.profile.update;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.event.UserEventType;
import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.core.support.ProfileCache;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.exception.ErrorCode;
import com.learning.user_service.gateway.DomainEventRecorder;
import com.learning.user_service.gateway.UserGateway;
import com.learning.user_service.gateway.oracle.user.entity.UserEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class UpdateProfileService implements Service<UpdateProfileServiceRequest, UpdateProfileServiceResponse> {

    private final UserGateway userGateway;
    private final DomainEventRecorder events;
    private final UserAccess userAccess;
    private final ProfileCache profileCache;

    public UpdateProfileService(UserGateway userGateway, DomainEventRecorder events, UserAccess userAccess,
                                ProfileCache profileCache) {
        this.userGateway = userGateway;
        this.events = events;
        this.userAccess = userAccess;
        this.profileCache = profileCache;
    }

    @Override
    @Transactional
    public UpdateProfileServiceResponse execute(UpdateProfileServiceRequest request) {
        UUID userId = request.getUserId();
        UserEntity user = userAccess.loadUsable(userId);
        if (request.getVersion() != null && request.getVersion() != user.getVersion()) {
            throw new ObjectOptimisticLockingFailureException(UserEntity.class, userId);
        }
        List<String> changes = new ArrayList<>();
        if (request.getFirstName() != null && !request.getFirstName().trim().equals(user.getFirstName())) {
            if (request.getFirstName().isBlank()) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "firstName must not be blank");
            }
            user.setFirstName(request.getFirstName().trim());
            changes.add("firstName");
        }
        if (request.getLastName() != null && !Objects.equals(request.getLastName().trim(), user.getLastName())) {
            user.setLastName(request.getLastName().isBlank() ? null : request.getLastName().trim());
            changes.add("lastName");
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userGateway.existsActiveByPhone(request.getPhone())) {
                throw new ApiException(ErrorCode.PHONE_ALREADY_REGISTERED, "An account with this phone already exists");
            }
            user.setPhone(request.getPhone());
            user.setPhoneVerifiedAt(null);
            changes.add("phone");
        }
        if (request.getLocale() != null && !request.getLocale().equals(user.getLocale())) {
            user.setLocale(request.getLocale());
            changes.add("locale");
        }
        if (!changes.isEmpty()) {
            userGateway.flush();
            events.record(userId, UserEventType.USER_UPDATED, Map.of(
                    "userId", userId.toString(),
                    "changes", changes,
                    "firstName", user.getFirstName(),
                    "lastName", Objects.toString(user.getLastName(), ""),
                    "locale", user.getLocale()));
            profileCache.evict(userId);
        }
        return UpdateProfileServiceResponse.builder().profile(UserProfile.from(user)).build();
    }
}
