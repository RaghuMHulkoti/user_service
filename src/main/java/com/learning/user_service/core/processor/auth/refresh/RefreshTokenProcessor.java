package com.learning.user_service.core.processor.auth.refresh;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.auth.refresh.RefreshTokenUsecase;
import com.learning.user_service.core.usecase.auth.refresh.RefreshTokenUsecaseRequest;
import com.learning.user_service.core.usecase.auth.refresh.RefreshTokenUsecaseResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenProcessor implements Processor<RefreshTokenProcessorRequest, RefreshTokenProcessorResponse> {

    private final RefreshTokenUsecase refreshTokenUsecase;

    public RefreshTokenProcessor(RefreshTokenUsecase refreshTokenUsecase) {
        this.refreshTokenUsecase = refreshTokenUsecase;
    }

    @Override
    public RefreshTokenProcessorResponse process(RefreshTokenProcessorRequest request) {
        RefreshTokenUsecaseResponse response = refreshTokenUsecase.execute(buildUsecaseRequest(request));
        return Optional.of(response)
                .map(RefreshTokenProcessor::buildProcessorResponse)
                .orElseThrow();
    }

    private static RefreshTokenUsecaseRequest buildUsecaseRequest(RefreshTokenProcessorRequest request) {
        return RefreshTokenUsecaseRequest.builder()
                .refreshToken(request.getRefreshToken())
                .build();
    }

    private static RefreshTokenProcessorResponse buildProcessorResponse(RefreshTokenUsecaseResponse response) {
        return RefreshTokenProcessorResponse.builder()
                .tokenPair(response.getTokenPair())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
