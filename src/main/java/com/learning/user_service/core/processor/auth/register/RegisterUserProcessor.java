package com.learning.user_service.core.processor.auth.register;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.register.RegisterUserUsecase;
import com.learning.user_service.core.usecase.auth.register.RegisterUserUsecaseRequest;
import com.learning.user_service.core.usecase.auth.register.RegisterUserUsecaseResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegisterUserProcessor implements Processor<RegisterUserProcessorRequest, RegisterUserProcessorResponse> {

    private final RegisterUserUsecase registerUserUsecase;

    public RegisterUserProcessor(RegisterUserUsecase registerUserUsecase) {
        this.registerUserUsecase = registerUserUsecase;
    }

    @Override
    public RegisterUserProcessorResponse process(RegisterUserProcessorRequest request) {
        RegisterUserUsecaseResponse usecaseResponse = registerUserUsecase.execute(buildRegisterUserUsecaseRequest(request));
        return Optional.of(usecaseResponse)
                .map(RegisterUserProcessor::buildRegisterUserProcessorResponse)
                .orElseThrow();
    }

    private static RegisterUserUsecaseRequest buildRegisterUserUsecaseRequest(RegisterUserProcessorRequest request) {
        return RegisterUserUsecaseRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .locale(request.getLocale())
                .build();
    }

    private static RegisterUserProcessorResponse buildRegisterUserProcessorResponse(RegisterUserUsecaseResponse response) {
        return RegisterUserProcessorResponse.builder()
                .registrationResult(response.getRegistrationResult())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
