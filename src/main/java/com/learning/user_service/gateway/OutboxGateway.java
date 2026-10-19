package com.learning.user_service.gateway;

import java.time.Instant;
import com.learning.user_service.gateway.oracle.outbox.entity.UserOutboxEntity;
import java.util.List;

public interface OutboxGateway {

    UserOutboxEntity save(UserOutboxEntity row);

    /** Locks a batch of pending rows; other pollers skip them. */
    List<UserOutboxEntity> lockPendingBatch(int limit);

    int deleteSentBefore(Instant cutoff);
}
