package com.learning.user_service.core.usecase.admin.setroles;

import com.learning.user_service.core.service.admin.setroles.SetUserRolesService;
import com.learning.user_service.core.service.admin.setroles.SetUserRolesServiceRequest;
import com.learning.user_service.core.service.admin.setroles.SetUserRolesServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SetUserRolesUsecase implements Usecase<SetUserRolesUsecaseRequest, SetUserRolesUsecaseResponse> {

    private final SetUserRolesService setUserRolesService;

    public SetUserRolesUsecase(SetUserRolesService setUserRolesService) {
        this.setUserRolesService = setUserRolesService;
    }

    @Override
    public SetUserRolesUsecaseResponse execute(SetUserRolesUsecaseRequest request) {
        SetUserRolesServiceResponse serviceResponse = setUserRolesService.execute(buildSetUserRolesServiceRequest(request));
        return Optional.of(serviceResponse)
                .map(SetUserRolesUsecase::buildSetUserRolesUsecaseResponse)
                .orElseThrow();
    }

    private static SetUserRolesServiceRequest buildSetUserRolesServiceRequest(SetUserRolesUsecaseRequest request) {
        return SetUserRolesServiceRequest.builder()
                .userId(request.getUserId())
                .roles(request.getRoles())
                .build();
    }

    private static SetUserRolesUsecaseResponse buildSetUserRolesUsecaseResponse(SetUserRolesServiceResponse response) {
        return SetUserRolesUsecaseResponse.builder()
                .profile(response.getProfile())
                .build();
    }
}
