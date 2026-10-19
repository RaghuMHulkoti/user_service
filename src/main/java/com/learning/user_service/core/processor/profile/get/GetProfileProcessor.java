package com.learning.user_service.core.processor.profile.get;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.profile.get.GetProfileUsecase;
import com.learning.user_service.core.usecase.profile.get.GetProfileUsecaseRequest;
import com.learning.user_service.core.usecase.profile.get.GetProfileUsecaseResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetProfileProcessor implements Processor<GetProfileProcessorRequest, GetProfileProcessorResponse> {

    private final GetProfileUsecase getProfileUsecase;

    public GetProfileProcessor(GetProfileUsecase getProfileUsecase) {
        this.getProfileUsecase = getProfileUsecase;
    }

    @Override
    public GetProfileProcessorResponse process(GetProfileProcessorRequest request) {
        GetProfileUsecaseResponse getProfileUsecaseResponse = getProfileUsecase.execute(buildGetProfileUsecaseRequest(request));
        return Optional.of(getProfileUsecaseResponse)
                .map(GetProfileProcessor::buildGetProfileProcessorResponse)
                .orElseThrow();
    }

    private static GetProfileUsecaseRequest buildGetProfileUsecaseRequest(GetProfileProcessorRequest request) {
        return GetProfileUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static GetProfileProcessorResponse buildGetProfileProcessorResponse(GetProfileUsecaseResponse response) {
        return GetProfileProcessorResponse.builder()
                .profile(response.getProfile())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
