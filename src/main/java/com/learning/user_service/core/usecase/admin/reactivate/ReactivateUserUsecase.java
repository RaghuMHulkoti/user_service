package com.learning.user_service.core.usecase.admin.reactivate;

import com.learning.user_service.core.service.admin.reactivate.ReactivateUserService;
import com.learning.user_service.core.service.admin.reactivate.ReactivateUserServiceRequest;
import com.learning.user_service.core.service.admin.reactivate.ReactivateUserServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReactivateUserUsecase implements Usecase<ReactivateUserUsecaseRequest, ReactivateUserUsecaseResponse> {

    private final ReactivateUserService reactivateUserService;

    public ReactivateUserUsecase(ReactivateUserService reactivateUserService) {
        this.reactivateUserService = reactivateUserService;
    }

    @Override
    public ReactivateUserUsecaseResponse execute(ReactivateUserUsecaseRequest request) {
        ReactivateUserServiceResponse serviceResponse = reactivateUserService.execute(buildReactivateUserServiceRequest(request));
        return Optional.of(serviceResponse)
                .map(ReactivateUserUsecase::buildReactivateUserUsecaseResponse)
                .orElseThrow();
    }

    private static ReactivateUserServiceRequest buildReactivateUserServiceRequest(ReactivateUserUsecaseRequest request) {
        return ReactivateUserServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static ReactivateUserUsecaseResponse buildReactivateUserUsecaseResponse(ReactivateUserServiceResponse response) {
        return ReactivateUserUsecaseResponse.builder()
                .profile(response.getProfile())
                .build();
    }
}
