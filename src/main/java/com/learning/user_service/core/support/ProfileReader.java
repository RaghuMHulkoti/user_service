package com.learning.user_service.core.support;

import com.learning.user_service.config.CacheConfig;
import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.exception.ApiException;
import com.learning.user_service.gateway.UserGateway;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Read side for profiles. Cache-aside on {@code user:{id}:profile}, TTL 5 min (TDD §16). */
@Component
public class ProfileReader {

    private final UserGateway userGateway;

    public ProfileReader(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USER_PROFILE_CACHE, key = "#userId + ':profile'")
    public UserProfile getProfile(UUID userId) {
        return userGateway.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .map(UserProfile::from)
                .orElseThrow(() -> ApiException.notFound("User"));
    }
}
