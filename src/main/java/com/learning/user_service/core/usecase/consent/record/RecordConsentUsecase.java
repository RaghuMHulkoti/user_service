package com.learning.user_service.core.usecase.consent.record;

import com.learning.user_service.core.service.consent.record.RecordConsentService;
import com.learning.user_service.core.service.consent.record.RecordConsentServiceRequest;
import com.learning.user_service.core.service.consent.record.RecordConsentServiceResponse;
import com.learning.user_service.core.usecase.Usecase;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RecordConsentUsecase implements Usecase<RecordConsentUsecaseRequest, RecordConsentUsecaseResponse> {

    private final RecordConsentService recordConsentService;

    public RecordConsentUsecase(RecordConsentService recordConsentService) {
        this.recordConsentService = recordConsentService;
    }

    @Override
    public RecordConsentUsecaseResponse execute(RecordConsentUsecaseRequest request) {
        RecordConsentServiceResponse recordConsentServiceResponse = recordConsentService.execute(buildRecordConsentServiceRequest(request));
        return Optional.of(recordConsentServiceResponse)
                .map(RecordConsentUsecase::buildRecordConsentUsecaseResponse)
                .orElseThrow();
    }

    private static RecordConsentServiceRequest buildRecordConsentServiceRequest(RecordConsentUsecaseRequest request) {
        return RecordConsentServiceRequest.builder()
                .userId(request.getUserId())
                .kind(request.getKind())
                .granted(request.getGranted())
                .clientIp(request.getClientIp())
                .build();
    }

    private static RecordConsentUsecaseResponse buildRecordConsentUsecaseResponse(RecordConsentServiceResponse response) {
        return RecordConsentUsecaseResponse.builder()
                .consent(response.getConsent())
                .build();
    }
}
