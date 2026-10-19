package com.learning.user_service.core.usecase.auth.logout;

import com.learning.user_service.core.service.auth.logout.LogoutService;
import com.learning.user_service.core.service.auth.logout.LogoutServiceRequest;
import com.learning.user_service.core.service.auth.logout.LogoutServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LogoutUsecase implements Usecase<LogoutUsecaseRequest, LogoutUsecaseResponse> {

    private final LogoutService logoutService;

    public LogoutUsecase(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @Override
    public LogoutUsecaseResponse execute(LogoutUsecaseRequest request) {
        LogoutServiceResponse response = logoutService.execute(buildServiceRequest(request));
        return Optional.of(response)
                .map(LogoutUsecase::buildUsecaseResponse)
                .orElseThrow();
    }

    private static LogoutServiceRequest buildServiceRequest(LogoutUsecaseRequest request) {
        return LogoutServiceRequest.builder()
                .userId(request.getUserId())
                .refreshToken(request.getRefreshToken())
                .allSessions(request.getAllSessions())
                .build();
    }

    private static LogoutUsecaseResponse buildUsecaseResponse(LogoutServiceResponse response) {
        return LogoutUsecaseResponse.builder()
                .success(response.isSuccess())
                .build();
    }
}
