package com.retailpos.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In-memory cache manager for the configuration-resolution cache (see
 * {@code configuration.service.ConfigurationServiceImpl}). A simple
 * {@link ConcurrentMapCacheManager} is sufficient for Phase 1 - no Redis/Caffeine needed yet.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Cache name used by {@code ConfigurationService#resolve}. */
    public static final String CONFIG_RESOLUTION_CACHE = "configResolution";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(CONFIG_RESOLUTION_CACHE);
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
