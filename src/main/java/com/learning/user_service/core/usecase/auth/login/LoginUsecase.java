package com.learning.user_service.core.usecase.auth.login;

import com.learning.user_service.core.service.auth.login.LoginService;
import com.learning.user_service.core.service.auth.login.LoginServiceRequest;
import com.learning.user_service.core.service.auth.login.LoginServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LoginUsecase implements Usecase<LoginUsecaseRequest, LoginUsecaseResponse> {

    private final LoginService loginService;

    public LoginUsecase(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public LoginUsecaseResponse execute(LoginUsecaseRequest request) {
        LoginServiceResponse response = loginService.execute(buildServiceRequest(request));
        return Optional.of(response)
                .map(LoginUsecase::buildUsecaseResponse)
                .orElseThrow();
    }

    private static LoginServiceRequest buildServiceRequest(LoginUsecaseRequest request) {
        return LoginServiceRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .clientIp(request.getClientIp())
                .build();
    }

    private static LoginUsecaseResponse buildUsecaseResponse(LoginServiceResponse response) {
        return LoginUsecaseResponse.builder()
                .tokenPair(response.getTokenPair())
                .build();
    }
}
