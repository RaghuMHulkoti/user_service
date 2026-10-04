package com.learning.user_service.domain.event;

/** Events published to {@code domain.user.events}. */
public enum UserEventType {
    USER_REGISTERED,
    USER_UPDATED,
    USER_DELETED,
    ADDRESS_UPDATED,
    EMAIL_VERIFICATION_REQUESTED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_CHANGED
}
