package com.learning.user_service.core.processor.auth.login;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.login.LoginUsecase;
import com.learning.user_service.core.usecase.auth.login.LoginUsecaseRequest;
import com.learning.user_service.core.usecase.auth.login.LoginUsecaseResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LoginProcessor implements Processor<LoginProcessorRequest, LoginProcessorResponse> {

    private final LoginUsecase loginUsecase;

    public LoginProcessor(LoginUsecase loginUsecase) {
        this.loginUsecase = loginUsecase;
    }

    @Override
    public LoginProcessorResponse process(LoginProcessorRequest request) {
        LoginUsecaseResponse response = loginUsecase.execute(buildUsecaseRequest(request));
        return Optional.of(response)
                .map(LoginProcessor::buildProcessorResponse)
                .orElseThrow();
    }

    private static LoginUsecaseRequest buildUsecaseRequest(LoginProcessorRequest request) {
        return LoginUsecaseRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .clientIp(request.getClientIp())
                .build();
    }

    private static LoginProcessorResponse buildProcessorResponse(LoginUsecaseResponse response) {
        return LoginProcessorResponse.builder()
                .tokenPair(response.getTokenPair())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
