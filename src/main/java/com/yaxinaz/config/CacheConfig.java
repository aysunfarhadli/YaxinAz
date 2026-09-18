package com.yaxinaz.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Spring Boot's default Caffeine autoconfiguration (with no explicit spec) creates unbounded caches
 * with no expiry - fine for nothing, dangerous for anything that must reflect writes. Statistics
 * (spec section 75) get a short TTL instead of manual @CacheEvict wiring into every issue/post/poll/
 * event write path - simpler and appropriate for a dashboard number that's allowed to be a couple of
 * minutes stale.
 */
@Configuration
public class CacheConfig {

    public static final String COMMUNITY_STATISTICS_CACHE = "communityStatistics";

    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheManagerCustomizer() {
        return cacheManager -> cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .maximumSize(1000));
    }
}
