package com.learning.user_service.web.model;

import jakarta.validation.constraints.Size;

/** {@code allSessions=true} signs the user out on every device. */
public record LogoutWebRequest(@Size(max = 200) String refreshToken, Boolean allSessions) {

    public boolean everywhere() {
        return Boolean.TRUE.equals(allSessions);
    }
}
