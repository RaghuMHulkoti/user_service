package com.learning.user_service.core.service.consent.record;

import com.learning.user_service.core.service.Service;
import com.learning.user_service.core.service.UserAccess;
import com.learning.user_service.gateway.ConsentGateway;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** GDPR/DPDP consent. Append-only: every decision is kept with time and IP. */
@Component
public class RecordConsentService implements Service<RecordConsentServiceRequest, RecordConsentServiceResponse> {

    private final ConsentGateway consentGateway;
    private final UserAccess userAccess;

    public RecordConsentService(ConsentGateway consentGateway, UserAccess userAccess) {
        this.consentGateway = consentGateway;
        this.userAccess = userAccess;
    }

    @Override
    @Transactional
    public RecordConsentServiceResponse execute(RecordConsentServiceRequest request) {
        userAccess.loadUsable(request.getUserId());
        ConsentLogEntity entry = new ConsentLogEntity();
        entry.setUserId(request.getUserId());
        entry.setKind(request.getKind());
        entry.setGranted(request.getGranted());
        entry.setIp(request.getClientIp());
        return RecordConsentServiceResponse.builder().consent(consentGateway.saveAndFlush(entry)).build();
    }
}
