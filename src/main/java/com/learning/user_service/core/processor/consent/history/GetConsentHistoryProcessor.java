package com.learning.user_service.core.processor.consent.history;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.consent.history.GetConsentHistoryUsecase;
import com.learning.user_service.core.usecase.consent.history.GetConsentHistoryUsecaseRequest;
import com.learning.user_service.core.usecase.consent.history.GetConsentHistoryUsecaseResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetConsentHistoryProcessor implements Processor<GetConsentHistoryProcessorRequest, GetConsentHistoryProcessorResponse> {

    private final GetConsentHistoryUsecase getConsentHistoryUsecase;

    public GetConsentHistoryProcessor(GetConsentHistoryUsecase getConsentHistoryUsecase) {
        this.getConsentHistoryUsecase = getConsentHistoryUsecase;
    }

    @Override
    public GetConsentHistoryProcessorResponse process(GetConsentHistoryProcessorRequest request) {
        GetConsentHistoryUsecaseResponse getConsentHistoryUsecaseResponse = getConsentHistoryUsecase.execute(buildGetConsentHistoryUsecaseRequest(request));
        return Optional.of(getConsentHistoryUsecaseResponse)
                .map(GetConsentHistoryProcessor::buildGetConsentHistoryProcessorResponse)
                .orElseThrow();
    }

    private static GetConsentHistoryUsecaseRequest buildGetConsentHistoryUsecaseRequest(GetConsentHistoryProcessorRequest request) {
        return GetConsentHistoryUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static GetConsentHistoryProcessorResponse buildGetConsentHistoryProcessorResponse(GetConsentHistoryUsecaseResponse response) {
        return GetConsentHistoryProcessorResponse.builder()
                .consents(response.getConsents())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
