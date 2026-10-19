package com.learning.user_service.core.processor.profile.update;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.profile.update.UpdateProfileUsecase;
import com.learning.user_service.core.usecase.profile.update.UpdateProfileUsecaseRequest;
import com.learning.user_service.core.usecase.profile.update.UpdateProfileUsecaseResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UpdateProfileProcessor implements Processor<UpdateProfileProcessorRequest, UpdateProfileProcessorResponse> {

    private final UpdateProfileUsecase updateProfileUsecase;

    public UpdateProfileProcessor(UpdateProfileUsecase updateProfileUsecase) {
        this.updateProfileUsecase = updateProfileUsecase;
    }

    @Override
    public UpdateProfileProcessorResponse process(UpdateProfileProcessorRequest request) {
        UpdateProfileUsecaseResponse updateProfileUsecaseResponse = updateProfileUsecase.execute(buildUpdateProfileUsecaseRequest(request));
        return Optional.of(updateProfileUsecaseResponse)
                .map(UpdateProfileProcessor::buildUpdateProfileProcessorResponse)
                .orElseThrow();
    }

    private static UpdateProfileUsecaseRequest buildUpdateProfileUsecaseRequest(UpdateProfileProcessorRequest request) {
        return UpdateProfileUsecaseRequest.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .locale(request.getLocale())
                .version(request.getVersion())
                .build();
    }

    private static UpdateProfileProcessorResponse buildUpdateProfileProcessorResponse(UpdateProfileUsecaseResponse response) {
        return UpdateProfileProcessorResponse.builder()
                .profile(response.getProfile())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
