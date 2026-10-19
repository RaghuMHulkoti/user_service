package com.learning.user_service.core.support;

import com.learning.user_service.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * Evicts {@code user:{id}:profile}. Runs after commit, so a concurrent read cannot put the
 * old data back. A Redis error is logged and ignored: the 5 min TTL bounds staleness.
 */
@Component
public class ProfileCache {

    private static final Logger log = LoggerFactory.getLogger(ProfileCache.class);

    private final CacheManager cacheManager;

    public ProfileCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public static String key(UUID userId) {
        return userId + ":profile";
    }

    public void evict(UUID userId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictNow(userId);
                }
            });
        } else {
            evictNow(userId);
        }
    }

    private void evictNow(UUID userId) {
        try {
            Cache cache = cacheManager.getCache(CacheConfig.USER_PROFILE_CACHE);
            if (cache != null) {
                cache.evict(key(userId));
            }
        } catch (RuntimeException ex) {
            log.warn("Profile cache evict failed for {}: {}", userId, ex.getMessage());
        }
    }
}
