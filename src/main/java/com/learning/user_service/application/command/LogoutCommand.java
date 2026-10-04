package com.learning.user_service.application.command;

import jakarta.validation.constraints.Size;

/** {@code allSessions=true} signs the user out on every device. */
public record LogoutCommand(@Size(max = 200) String refreshToken, Boolean allSessions) {

    public boolean everywhere() {
        return Boolean.TRUE.equals(allSessions);
    }
}
