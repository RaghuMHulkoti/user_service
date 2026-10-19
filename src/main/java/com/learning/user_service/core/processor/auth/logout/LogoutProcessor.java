package com.learning.user_service.core.processor.auth.logout;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.logout.LogoutUsecase;
import com.learning.user_service.core.usecase.auth.logout.LogoutUsecaseRequest;
import com.learning.user_service.core.usecase.auth.logout.LogoutUsecaseResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LogoutProcessor implements Processor<LogoutProcessorRequest, LogoutProcessorResponse> {

    private final LogoutUsecase logoutUsecase;

    public LogoutProcessor(LogoutUsecase logoutUsecase) {
        this.logoutUsecase = logoutUsecase;
    }

    @Override
    public LogoutProcessorResponse process(LogoutProcessorRequest request) {
        LogoutUsecaseResponse response = logoutUsecase.execute(buildUsecaseRequest(request));
        return Optional.of(response)
                .map(LogoutProcessor::buildProcessorResponse)
                .orElseThrow();
    }

    private static LogoutUsecaseRequest buildUsecaseRequest(LogoutProcessorRequest request) {
        return LogoutUsecaseRequest.builder()
                .userId(request.getUserId())
                .refreshToken(request.getRefreshToken())
                .allSessions(request.getAllSessions())
                .build();
    }

    private static LogoutProcessorResponse buildProcessorResponse(LogoutUsecaseResponse response) {
        return LogoutProcessorResponse.builder()
                .success(response.isSuccess())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
