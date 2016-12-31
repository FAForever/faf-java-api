package com.faforever.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.faforever.api.config.security.oauth2.OAuthClientDetailsService.CLIENTS_CACHE_NAME;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@EnableCaching
@Configuration
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(
        // Elide caches
        new GuavaCache("default", newBuilder().expireAfterWrite(5, SECONDS).build()),
        new GuavaCache("achievement_definition", newBuilder().expireAfterWrite(60, MINUTES).build()),
        new GuavaCache("event_definition", newBuilder().expireAfterWrite(60, MINUTES).build()),
        // Other caches
        new GuavaCache(CLIENTS_CACHE_NAME, newBuilder().expireAfterWrite(5, SECONDS).build())
    ));
    return cacheManager;
  }
}
