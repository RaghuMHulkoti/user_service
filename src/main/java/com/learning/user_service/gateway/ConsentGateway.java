package com.learning.user_service.gateway;

import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;
import java.util.List;
import java.util.UUID;

public interface ConsentGateway {

    /** Latest decision per consent kind. */
    List<ConsentLogEntity> findLatestPerKind(UUID userId);

    List<ConsentLogEntity> findHistory(UUID userId);

    ConsentLogEntity saveAndFlush(ConsentLogEntity consent);

    /** Right to erasure: drops the stored IP from every consent row of the user. */
    void clearIpByUserId(UUID userId);
}
