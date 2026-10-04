package com.learning.user_service.application.query;

import com.learning.user_service.infrastructure.persistence.entity.ConsentLogEntity;

import java.time.Instant;

public record ConsentView(String kind, boolean granted, Instant recordedAt) {

    public static ConsentView from(ConsentLogEntity c) {
        return new ConsentView(c.getKind(), c.isGranted(), c.getCreatedAt());
    }
}
