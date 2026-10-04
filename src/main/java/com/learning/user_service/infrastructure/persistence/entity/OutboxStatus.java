package com.learning.user_service.infrastructure.persistence.entity;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
