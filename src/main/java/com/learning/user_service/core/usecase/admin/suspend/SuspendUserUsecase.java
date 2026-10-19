package com.learning.user_service.core.usecase.admin.suspend;

import com.learning.user_service.core.service.admin.suspend.SuspendUserService;
import com.learning.user_service.core.service.admin.suspend.SuspendUserServiceRequest;
import com.learning.user_service.core.service.admin.suspend.SuspendUserServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SuspendUserUsecase implements Usecase<SuspendUserUsecaseRequest, SuspendUserUsecaseResponse> {

    private final SuspendUserService suspendUserService;

    public SuspendUserUsecase(SuspendUserService suspendUserService) {
        this.suspendUserService = suspendUserService;
    }

    @Override
    public SuspendUserUsecaseResponse execute(SuspendUserUsecaseRequest request) {
        SuspendUserServiceResponse serviceResponse = suspendUserService.execute(buildSuspendUserServiceRequest(request));
        return Optional.of(serviceResponse)
                .map(SuspendUserUsecase::buildSuspendUserUsecaseResponse)
                .orElseThrow();
    }

    private static SuspendUserServiceRequest buildSuspendUserServiceRequest(SuspendUserUsecaseRequest request) {
        return SuspendUserServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static SuspendUserUsecaseResponse buildSuspendUserUsecaseResponse(SuspendUserServiceResponse response) {
        return SuspendUserUsecaseResponse.builder()
                .profile(response.getProfile())
                .build();
    }
}
