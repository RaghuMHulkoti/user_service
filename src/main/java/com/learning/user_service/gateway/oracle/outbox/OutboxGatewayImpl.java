package com.learning.user_service.gateway.oracle.outbox;

import java.time.Instant;
import com.learning.user_service.gateway.OutboxGateway;
import com.learning.user_service.gateway.oracle.outbox.entity.UserOutboxEntity;
import com.learning.user_service.gateway.oracle.outbox.repository.UserOutboxRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OutboxGatewayImpl implements OutboxGateway {

    private final com.learning.user_service.gateway.oracle.outbox.repository.UserOutboxRepository repository;

    public OutboxGatewayImpl(com.learning.user_service.gateway.oracle.outbox.repository.UserOutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserOutboxEntity save(UserOutboxEntity row) {
        return repository.save(row);
    }

    @Override
    public List<UserOutboxEntity> lockPendingBatch(int limit) {
        return repository.lockPendingBatch(limit);
    }

    @Override
    public int deleteSentBefore(Instant cutoff) {
        return repository.deleteSentBefore(cutoff);
    }
}
