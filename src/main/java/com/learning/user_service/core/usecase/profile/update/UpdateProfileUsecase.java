package com.learning.user_service.core.usecase.profile.update;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.service.profile.update.UpdateProfileService;
import com.learning.user_service.core.service.profile.update.UpdateProfileServiceRequest;
import com.learning.user_service.core.service.profile.update.UpdateProfileServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UpdateProfileUsecase implements Usecase<UpdateProfileUsecaseRequest, UpdateProfileUsecaseResponse> {

    private final UpdateProfileService updateProfileService;

    public UpdateProfileUsecase(UpdateProfileService updateProfileService) {
        this.updateProfileService = updateProfileService;
    }

    @Override
    public UpdateProfileUsecaseResponse execute(UpdateProfileUsecaseRequest request) {
        UpdateProfileServiceResponse updateProfileServiceResponse = updateProfileService.execute(buildUpdateProfileServiceRequest(request));
        return Optional.of(updateProfileServiceResponse)
                .map(UpdateProfileUsecase::buildUpdateProfileUsecaseResponse)
                .orElseThrow();
    }

    private static UpdateProfileServiceRequest buildUpdateProfileServiceRequest(UpdateProfileUsecaseRequest request) {
        return UpdateProfileServiceRequest.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .locale(request.getLocale())
                .version(request.getVersion())
                .build();
    }

    private static UpdateProfileUsecaseResponse buildUpdateProfileUsecaseResponse(UpdateProfileServiceResponse response) {
        return UpdateProfileUsecaseResponse.builder()
                .profile(response.getProfile())
                .build();
    }
}
