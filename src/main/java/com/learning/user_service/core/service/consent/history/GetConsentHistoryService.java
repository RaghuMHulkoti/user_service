package com.learning.user_service.core.service.consent.history;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.gateway.ConsentGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetConsentHistoryService implements Service<GetConsentHistoryServiceRequest, GetConsentHistoryServiceResponse> {

    private final ConsentGateway consentGateway;
    private final UserAccess userAccess;

    public GetConsentHistoryService(ConsentGateway consentGateway, UserAccess userAccess) {
        this.consentGateway = consentGateway;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional(readOnly = true)
    public GetConsentHistoryServiceResponse execute(GetConsentHistoryServiceRequest request) {
        userAccess.loadUsable(request.getUserId());
        return GetConsentHistoryServiceResponse.builder()
                .consents(consentGateway.findHistory(request.getUserId()))
                .build();
    }
}
