package com.learning.user_service.core.usecase.auth.refresh;

import com.learning.user_service.core.service.auth.refresh.RefreshTokenService;
import com.learning.user_service.core.service.auth.refresh.RefreshTokenServiceRequest;
import com.learning.user_service.core.service.auth.refresh.RefreshTokenServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenUsecase implements Usecase<RefreshTokenUsecaseRequest, RefreshTokenUsecaseResponse> {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenUsecase(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public RefreshTokenUsecaseResponse execute(RefreshTokenUsecaseRequest request) {
        RefreshTokenServiceResponse response = refreshTokenService.execute(buildServiceRequest(request));
        return Optional.of(response)
                .map(RefreshTokenUsecase::buildUsecaseResponse)
                .orElseThrow();
    }

    private static RefreshTokenServiceRequest buildServiceRequest(RefreshTokenUsecaseRequest request) {
        return RefreshTokenServiceRequest.builder()
                .refreshToken(request.getRefreshToken())
                .build();
    }

    private static RefreshTokenUsecaseResponse buildUsecaseResponse(RefreshTokenServiceResponse response) {
        return RefreshTokenUsecaseResponse.builder()
                .tokenPair(response.getTokenPair())
                .build();
    }
}
