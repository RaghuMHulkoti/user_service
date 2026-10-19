package com.learning.user_service.core.service;

import com.learning.user_service.web.model.RecordConsentWebRequest;
import com.learning.user_service.web.response.ConsentResponse;
import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import com.learning.user_service.gateway.oracle.consent.repository.ConsentLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** GDPR/DPDP consent. Append-only: every decision is kept with time and IP. */
@Service
public class ConsentService {

    private final ConsentLogRepository repository;
    private final UserAccess userAccess;

    public ConsentService(ConsentLogRepository repository, UserAccess userAccess) {
        this.repository = repository;
        this.userAccess = userAccess;
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> current(UUID userId) {
        userAccess.loadUsable(userId);
        return repository.findLatestPerKind(userId).stream().map(ConsentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> history(UUID userId) {
        userAccess.loadUsable(userId);
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(ConsentResponse::from).toList();
    }

    @Transactional
    public ConsentResponse record(UUID userId, RecordConsentWebRequest cmd, String clientIp) {
        userAccess.loadUsable(userId);
        ConsentLogEntity entry = new ConsentLogEntity();
        entry.setUserId(userId);
        entry.setKind(cmd.kind());
        entry.setGranted(cmd.granted());
        entry.setIp(clientIp);
        return ConsentResponse.from(repository.saveAndFlush(entry));
    }
}
