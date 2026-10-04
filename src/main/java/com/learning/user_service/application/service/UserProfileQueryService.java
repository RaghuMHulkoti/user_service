package com.learning.user_service.application.service;

import com.learning.user_service.domain.model.UserStatus;
import com.learning.user_service.infrastructure.cache.CacheConfig;
import com.learning.user_service.infrastructure.persistence.repository.UserRepository;
import com.learning.user_service.presentation.error.ApiException;
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
