package com.learning.user_service.web.mapper;

import com.learning.user_service.core.processor.consent.current.GetCurrentConsentsProcessorRequest;
import com.learning.user_service.core.processor.consent.current.GetCurrentConsentsProcessorResponse;
import com.learning.user_service.core.processor.consent.history.GetConsentHistoryProcessorRequest;
import com.learning.user_service.core.processor.consent.history.GetConsentHistoryProcessorResponse;
import com.learning.user_service.core.processor.consent.record.RecordConsentProcessorRequest;
import com.learning.user_service.core.processor.consent.record.RecordConsentProcessorResponse;
import com.learning.user_service.web.model.RecordConsentWebRequest;
import com.learning.user_service.web.response.ConsentResponse;

import java.util.List;
import java.util.UUID;

public final class ConsentMapper {

    private ConsentMapper() {
    }

    public static GetCurrentConsentsProcessorRequest toGetCurrentConsentsRequest(UUID userId) {
        return GetCurrentConsentsProcessorRequest.builder().userId(userId).build();
    }

    public static List<ConsentResponse> mapGetCurrentConsentsResponse(GetCurrentConsentsProcessorResponse response) {
        return response.getConsents().stream().map(ConsentResponse::from).toList();
    }

    public static GetConsentHistoryProcessorRequest toGetConsentHistoryRequest(UUID userId) {
        return GetConsentHistoryProcessorRequest.builder().userId(userId).build();
    }

    public static List<ConsentResponse> mapGetConsentHistoryResponse(GetConsentHistoryProcessorResponse response) {
        return response.getConsents().stream().map(ConsentResponse::from).toList();
    }

    public static RecordConsentProcessorRequest toRecordConsentRequest(UUID userId, RecordConsentWebRequest request,
                                                                       String clientIp) {
        return RecordConsentProcessorRequest.builder()
                .userId(userId)
                .kind(request.kind())
                .granted(request.granted())
                .clientIp(clientIp)
                .build();
    }

    public static ConsentResponse mapRecordConsentResponse(RecordConsentProcessorResponse response) {
        return ConsentResponse.from(response.getConsent());
    }
}
