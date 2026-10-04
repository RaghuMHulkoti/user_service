package com.learning.user_service.domain.model;

public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    DELETED;

    public boolean canLogIn() {
        return this == PENDING_VERIFICATION || this == ACTIVE;
    }
}
