package com.learning.user_service.core.processor.admin.setroles;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.admin.setroles.SetUserRolesUsecase;
import com.learning.user_service.core.usecase.admin.setroles.SetUserRolesUsecaseRequest;
import com.learning.user_service.core.usecase.admin.setroles.SetUserRolesUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SetUserRolesProcessor implements Processor<SetUserRolesProcessorRequest, SetUserRolesProcessorResponse> {

    private final SetUserRolesUsecase setUserRolesUsecase;

    public SetUserRolesProcessor(SetUserRolesUsecase setUserRolesUsecase) {
        this.setUserRolesUsecase = setUserRolesUsecase;
    }

    @Override
    public SetUserRolesProcessorResponse process(SetUserRolesProcessorRequest request) {
        SetUserRolesUsecaseResponse usecaseResponse = setUserRolesUsecase.execute(buildSetUserRolesUsecaseRequest(request));
        return Optional.of(usecaseResponse)
                .map(SetUserRolesProcessor::buildSetUserRolesProcessorResponse)
                .orElseThrow();
    }

    private static SetUserRolesUsecaseRequest buildSetUserRolesUsecaseRequest(SetUserRolesProcessorRequest request) {
        return SetUserRolesUsecaseRequest.builder()
                .userId(request.getUserId())
                .roles(request.getRoles())
                .build();
    }

    private static SetUserRolesProcessorResponse buildSetUserRolesProcessorResponse(SetUserRolesUsecaseResponse response) {
        return SetUserRolesProcessorResponse.builder()
                .profile(response.getProfile())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
