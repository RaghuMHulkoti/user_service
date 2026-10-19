package com.learning.user_service.core.usecase.consent.history;

import com.learning.user_service.core.service.consent.history.GetConsentHistoryService;
import com.learning.user_service.core.service.consent.history.GetConsentHistoryServiceRequest;
import com.learning.user_service.core.service.consent.history.GetConsentHistoryServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetConsentHistoryUsecase implements Usecase<GetConsentHistoryUsecaseRequest, GetConsentHistoryUsecaseResponse> {

    private final GetConsentHistoryService getConsentHistoryService;

    public GetConsentHistoryUsecase(GetConsentHistoryService getConsentHistoryService) {
        this.getConsentHistoryService = getConsentHistoryService;
    }

    @Override
    public GetConsentHistoryUsecaseResponse execute(GetConsentHistoryUsecaseRequest request) {
        GetConsentHistoryServiceResponse getConsentHistoryServiceResponse = getConsentHistoryService.execute(buildGetConsentHistoryServiceRequest(request));
        return Optional.of(getConsentHistoryServiceResponse)
                .map(GetConsentHistoryUsecase::buildGetConsentHistoryUsecaseResponse)
                .orElseThrow();
    }

    private static GetConsentHistoryServiceRequest buildGetConsentHistoryServiceRequest(GetConsentHistoryUsecaseRequest request) {
        return GetConsentHistoryServiceRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static GetConsentHistoryUsecaseResponse buildGetConsentHistoryUsecaseResponse(GetConsentHistoryServiceResponse response) {
        return GetConsentHistoryUsecaseResponse.builder()
                .consents(response.getConsents())
                .build();
    }
}
