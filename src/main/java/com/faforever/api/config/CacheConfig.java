package com.faforever.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.faforever.api.featuredmods.FeaturedModService.FEATURED_MODS_CACHE_NAME;
import static com.faforever.api.featuredmods.FeaturedModService.FEATURED_MOD_FILES_CACHE_NAME;
import static com.faforever.api.leaderboard.LeaderboardService.LEADERBOARD_GLOBAL_CACHE_NAME;
import static com.faforever.api.leaderboard.LeaderboardService.LEADERBOARD_RANKED_1V1_CACHE_NAME;
import static com.faforever.api.security.OAuthClientDetailsService.CLIENTS_CACHE_NAME;
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
        new GuavaCache("achievement", newBuilder().expireAfterWrite(60, MINUTES).build()),
        new GuavaCache("eventDefinition", newBuilder().expireAfterWrite(60, MINUTES).build()),
        new GuavaCache("achievementDefinition", newBuilder().expireAfterWrite(60, MINUTES).build()),
        // Other caches
        new GuavaCache(LEADERBOARD_RANKED_1V1_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
        new GuavaCache(LEADERBOARD_GLOBAL_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
        new GuavaCache(FEATURED_MODS_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
        new GuavaCache(FEATURED_MOD_FILES_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
        new GuavaCache(CLIENTS_CACHE_NAME, newBuilder().expireAfterWrite(5, SECONDS).build())
    ));
    return cacheManager;
  }
}
