package com.learning.user_service.application.service;

import com.learning.user_service.application.command.RecordConsentCommand;
import com.learning.user_service.application.query.ConsentView;
import com.learning.user_service.infrastructure.persistence.entity.ConsentLogEntity;
import com.learning.user_service.infrastructure.persistence.repository.ConsentLogRepository;
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
    public List<ConsentView> current(UUID userId) {
        userAccess.loadUsable(userId);
        return repository.findLatestPerKind(userId).stream().map(ConsentView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentView> history(UUID userId) {
        userAccess.loadUsable(userId);
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(ConsentView::from).toList();
    }

    @Transactional
    public ConsentView record(UUID userId, RecordConsentCommand cmd, String clientIp) {
        userAccess.loadUsable(userId);
        ConsentLogEntity entry = new ConsentLogEntity();
        entry.setUserId(userId);
        entry.setKind(cmd.kind());
        entry.setGranted(cmd.granted());
        entry.setIp(clientIp);
        return ConsentView.from(repository.saveAndFlush(entry));
    }
}
