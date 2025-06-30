package com.banking.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration Class </br>
 * I use Caffeine as the caching provider here since it is single node application, and it's a simple demo. </br>
 * Note: If in distribution environment, we need to use Hazelcast or Redis as the caching provider.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    @Value("${app.cache.initial-capacity:200}")
    private int initialCapacity;
    
    @Value("${app.cache.maximum-size:10000}")
    private int maximumSize;
    
    @Value("${app.cache.expire-after-write-minutes:30}")
    private int expireAfterWriteMinutes;
    
    @Value("${app.cache.expire-after-access-minutes:10}")
    private int expireAfterAccessMinutes;

    @Bean
    public CacheManager cacheManager() {
        logger.info("Initializing cache manager..");
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        logger.info("Building Caffeine cache with parameters: initialCapacity {}, maximumSize {}, expireAfterWriteMinutes {}, expireAfterAccessMinutes {}", initialCapacity, maximumSize, expireAfterWriteMinutes, expireAfterAccessMinutes);
        return Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                .expireAfterAccess(expireAfterAccessMinutes, TimeUnit.MINUTES);
    }
} 