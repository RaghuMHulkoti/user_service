package com.learning.user_service.domain.port;

import com.learning.user_service.domain.model.RoleCode;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

/** Creates signed access tokens. */
public interface AccessTokenFactory {

    IssuedAccessToken issue(UUID userId, Collection<RoleCode> roles);

    record IssuedAccessToken(String value, Duration ttl) {
    }
}
