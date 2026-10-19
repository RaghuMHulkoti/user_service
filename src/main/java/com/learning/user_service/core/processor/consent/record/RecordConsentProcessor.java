package com.learning.user_service.core.processor.consent.record;

import com.learning.user_service.common.StatusCode;
import com.learning.user_service.core.processor.Processor;
import com.learning.user_service.core.usecase.consent.record.RecordConsentUsecase;
import com.learning.user_service.core.usecase.consent.record.RecordConsentUsecaseRequest;
import com.learning.user_service.core.usecase.consent.record.RecordConsentUsecaseResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RecordConsentProcessor implements Processor<RecordConsentProcessorRequest, RecordConsentProcessorResponse> {

    private final RecordConsentUsecase recordConsentUsecase;

    public RecordConsentProcessor(RecordConsentUsecase recordConsentUsecase) {
        this.recordConsentUsecase = recordConsentUsecase;
    }

    @Override
    public RecordConsentProcessorResponse process(RecordConsentProcessorRequest request) {
        RecordConsentUsecaseResponse recordConsentUsecaseResponse = recordConsentUsecase.execute(buildRecordConsentUsecaseRequest(request));
        return Optional.of(recordConsentUsecaseResponse)
                .map(RecordConsentProcessor::buildRecordConsentProcessorResponse)
                .orElseThrow();
    }

    private static RecordConsentUsecaseRequest buildRecordConsentUsecaseRequest(RecordConsentProcessorRequest request) {
        return RecordConsentUsecaseRequest.builder()
                .userId(request.getUserId())
                .kind(request.getKind())
                .granted(request.getGranted())
                .clientIp(request.getClientIp())
                .build();
    }

    private static RecordConsentProcessorResponse buildRecordConsentProcessorResponse(RecordConsentUsecaseResponse response) {
        return RecordConsentProcessorResponse.builder()
                .consent(response.getConsent())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }
}
