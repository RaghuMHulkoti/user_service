package com.learning.user_service.core.usecase.consent.current;

import com.learning.user_service.core.service.consent.current.GetCurrentConsentsService;
import com.learning.user_service.core.service.consent.current.GetCurrentConsentsServiceRequest;
import com.learning.user_service.core.service.consent.current.GetCurrentConsentsServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetCurrentConsentsUsecase implements Usecase<GetCurrentConsentsUsecaseRequest, GetCurrentConsentsUsecaseResponse> {

    private final GetCurrentConsentsService getCurrentConsentsService;

    public GetCurrentConsentsUsecase(GetCurrentConsentsService getCurrentConsentsService) {
        this.getCurrentConsentsService = getCurrentConsentsService;
    }

    @Override
    public GetCurrentConsentsUsecaseResponse execute(GetCurrentConsentsUsecaseRequest request) {
        GetCurrentConsentsServiceResponse getCurrentConsentsServiceResponse = getCurrentConsentsService.execute(buildGetCurrentConsentsServiceRequest(request));
        return Optional.of(getCurrentConsentsServiceResponse)
                .map(GetCurrentConsentsUsecase::buildGetCurrentConsentsUsecaseResponse)
                .orElseThrow();
    }

    private static GetCurrentConsentsServiceRequest buildGetCurrentConsentsServiceRequest(GetCurrentConsentsUsecaseRequest request) {
        return GetCurrentConsentsServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static GetCurrentConsentsUsecaseResponse buildGetCurrentConsentsUsecaseResponse(GetCurrentConsentsServiceResponse response) {
        return GetCurrentConsentsUsecaseResponse.builder()
                .consents(response.getConsents())
                .build();
    }
}
