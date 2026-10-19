package com.learning.user_service.core.processor.consent.current;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.consent.current.GetCurrentConsentsUsecase;
import com.learning.user_service.core.usecase.consent.current.GetCurrentConsentsUsecaseRequest;
import com.learning.user_service.core.usecase.consent.current.GetCurrentConsentsUsecaseResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetCurrentConsentsProcessor implements Processor<GetCurrentConsentsProcessorRequest, GetCurrentConsentsProcessorResponse> {

    private final GetCurrentConsentsUsecase getCurrentConsentsUsecase;

    public GetCurrentConsentsProcessor(GetCurrentConsentsUsecase getCurrentConsentsUsecase) {
        this.getCurrentConsentsUsecase = getCurrentConsentsUsecase;
    }

    @Override
    public GetCurrentConsentsProcessorResponse process(GetCurrentConsentsProcessorRequest request) {
        GetCurrentConsentsUsecaseResponse getCurrentConsentsUsecaseResponse = getCurrentConsentsUsecase.execute(buildGetCurrentConsentsUsecaseRequest(request));
        return Optional.of(getCurrentConsentsUsecaseResponse)
                .map(GetCurrentConsentsProcessor::buildGetCurrentConsentsProcessorResponse)
                .orElseThrow();
    }

    private static GetCurrentConsentsUsecaseRequest buildGetCurrentConsentsUsecaseRequest(GetCurrentConsentsProcessorRequest request) {
        return GetCurrentConsentsUsecaseRequest.builder()
                .userId(request.getUserId())
                .build();
    }

    private static GetCurrentConsentsProcessorResponse buildGetCurrentConsentsProcessorResponse(GetCurrentConsentsUsecaseResponse response) {
        return GetCurrentConsentsProcessorResponse.builder()
                .consents(response.getConsents())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
