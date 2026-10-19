package com.learning.user_service.core.usecase.auth.register;

import com.learning.user_service.core.usecase.Usecase;
import com.learning.user_service.core.service.auth.register.RegisterUserService;
import com.learning.user_service.core.service.auth.register.RegisterUserServiceRequest;
import com.learning.user_service.core.service.auth.register.RegisterUserServiceResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegisterUserUsecase implements Usecase<RegisterUserUsecaseRequest, RegisterUserUsecaseResponse> {

    private final RegisterUserService registerUserService;

    public RegisterUserUsecase(RegisterUserService registerUserService) {
        this.registerUserService = registerUserService;
    }

    @Override
    public RegisterUserUsecaseResponse execute(RegisterUserUsecaseRequest request) {
        RegisterUserServiceResponse serviceResponse = registerUserService.execute(buildRegisterUserServiceRequest(request));
        return Optional.of(serviceResponse)
                .map(RegisterUserUsecase::buildRegisterUserUsecaseResponse)
                .orElseThrow();
    }

    private static RegisterUserServiceRequest buildRegisterUserServiceRequest(RegisterUserUsecaseRequest request) {
        return RegisterUserServiceRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .locale(request.getLocale())
                .build();
    }

    private static RegisterUserUsecaseResponse buildRegisterUserUsecaseResponse(RegisterUserServiceResponse response) {
        return RegisterUserUsecaseResponse.builder()
                .registrationResult(response.getRegistrationResult())
                .build();
    }
}
