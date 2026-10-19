package com.learning.user_service.core.service;

import com.learning.user_service.core.domain.UserProfile;
import com.learning.user_service.core.domain.model.UserStatus;
import com.learning.user_service.config.CacheConfig;
import com.learning.user_service.gateway.oracle.user.repository.UserRepository;
import com.learning.user_service.exception.ApiException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Read side for profiles. Cache-aside on {@code user:{id}:profile}, TTL 5 min (TDD §16). */
@Service
public class UserProfileQueryService {

    private final UserRepository userRepository;

    public UserProfileQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USER_PROFILE_CACHE, key = "#userId + ':profile'")
    public UserProfile getProfile(UUID userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .map(UserProfile::from)
                .orElseThrow(() -> ApiException.notFound("User"));
    }
}
