package com.learning.user_service.infrastructure.cache;

import com.learning.user_service.application.service.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

/**
 * Profile cache {@code user:{id}:profile}, TTL 5 min, evicted on update (TDD §8.2, §16.2).
 * Cache errors are logged and skipped: if Redis is down, reads fall back to the DB (§16.4).
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String USER_PROFILE_CACHE = "user-profile";
    private static final Duration PROFILE_TTL = Duration.ofMinutes(5);
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public RedisCacheManagerBuilderCustomizer userProfileCacheCustomizer(JsonMapper jsonMapper) {
        RedisCacheConfiguration profileConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(PROFILE_TTL)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "user:")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(jsonMapper, UserProfile.class)));
        return builder -> builder.withCacheConfiguration(USER_PROFILE_CACHE, profileConfig);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Cache get failed for {}: {}", key, ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                log.warn("Cache put failed for {}: {}", key, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                log.warn("Cache evict failed for {}: {}", key, ex.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.warn("Cache clear failed: {}", ex.getMessage());
            }
        };
    }
}
