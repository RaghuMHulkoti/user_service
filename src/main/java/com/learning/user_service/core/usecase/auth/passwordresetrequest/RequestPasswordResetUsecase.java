package com.learning.user_service.core.usecase.auth.passwordresetrequest;

import com.learning.user_service.core.service.auth.passwordresetrequest.RequestPasswordResetService;
import com.learning.user_service.core.service.auth.passwordresetrequest.RequestPasswordResetServiceRequest;
import com.learning.user_service.core.service.auth.passwordresetrequest.RequestPasswordResetServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestPasswordResetUsecase implements Usecase<RequestPasswordResetUsecaseRequest, RequestPasswordResetUsecaseResponse> {

    private final RequestPasswordResetService requestPasswordResetService;

    public RequestPasswordResetUsecase(RequestPasswordResetService requestPasswordResetService) {
        this.requestPasswordResetService = requestPasswordResetService;
    }

    @Override
    public RequestPasswordResetUsecaseResponse execute(RequestPasswordResetUsecaseRequest request) {
        RequestPasswordResetServiceResponse response = requestPasswordResetService.execute(buildServiceRequest(request));
        return Optional.of(response)
                .map(RequestPasswordResetUsecase::buildUsecaseResponse)
                .orElseThrow();
    }

    private static RequestPasswordResetServiceRequest buildServiceRequest(RequestPasswordResetUsecaseRequest request) {
        return RequestPasswordResetServiceRequest.builder()
                .email(request.getEmail())
                .build();
    }

    private static RequestPasswordResetUsecaseResponse buildUsecaseResponse(RequestPasswordResetServiceResponse response) {
        return RequestPasswordResetUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
