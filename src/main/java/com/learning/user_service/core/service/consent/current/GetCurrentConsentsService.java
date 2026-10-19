package com.learning.user_service.core.service.consent.current;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.gateway.ConsentGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetCurrentConsentsService implements Service<GetCurrentConsentsServiceRequest, GetCurrentConsentsServiceResponse> {

    private final ConsentGateway consentGateway;
    private final UserAccess userAccess;

    public GetCurrentConsentsService(ConsentGateway consentGateway, UserAccess userAccess) {
        this.consentGateway = consentGateway;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional(readOnly = true)
    public GetCurrentConsentsServiceResponse execute(GetCurrentConsentsServiceRequest request) {
        userAccess.loadUsable(request.getUserId());
        return GetCurrentConsentsServiceResponse.builder()
                .consents(consentGateway.findLatestPerKind(request.getUserId()))
                .build();
    }
}
