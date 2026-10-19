package com.learning.user_service.web.response;

import com.learning.user_service.gateway.oracle.consent.entity.ConsentLogEntity;

import java.time.Instant;

public record ConsentResponse(String kind, boolean granted, Instant recordedAt) {

    public static ConsentResponse from(ConsentLogEntity c) {
        return new ConsentResponse(c.getKind(), c.isGranted(), c.getCreatedAt());
    }
}
