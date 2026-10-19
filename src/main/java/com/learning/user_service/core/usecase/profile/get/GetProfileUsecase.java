package com.learning.user_service.core.usecase.profile.get;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.service.profile.get.GetProfileService;
import com.learning.user_service.core.service.profile.get.GetProfileServiceRequest;
import com.learning.user_service.core.service.profile.get.GetProfileServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetProfileUsecase implements Usecase<GetProfileUsecaseRequest, GetProfileUsecaseResponse> {

    private final GetProfileService getProfileService;

    public GetProfileUsecase(GetProfileService getProfileService) {
        this.getProfileService = getProfileService;
    }

    @Override
    public GetProfileUsecaseResponse execute(GetProfileUsecaseRequest request) {
        GetProfileServiceResponse getProfileServiceResponse = getProfileService.execute(buildGetProfileServiceRequest(request));
        return Optional.of(getProfileServiceResponse)
                .map(GetProfileUsecase::buildGetProfileUsecaseResponse)
                .orElseThrow();
    }

    private static GetProfileServiceRequest buildGetProfileServiceRequest(GetProfileUsecaseRequest request) {
        return GetProfileServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static GetProfileUsecaseResponse buildGetProfileUsecaseResponse(GetProfileServiceResponse response) {
        return GetProfileUsecaseResponse.builder()
                .profile(response.getProfile())
                .build();
    }
}
