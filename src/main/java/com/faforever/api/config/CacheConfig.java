package com.faforever.api.config;

import com.faforever.api.config.elide.ElideConfig;
import com.faforever.api.dto.Achievement;
import com.faforever.api.dto.Avatar;
import com.faforever.api.dto.AvatarAssignment;
import com.faforever.api.dto.Clan;
import com.faforever.api.dto.Event;
import com.faforever.api.dto.FeaturedMod;
import com.faforever.api.dto.Map;
import com.faforever.api.dto.MapStatistics;
import com.faforever.api.dto.MapVersion;
import com.faforever.api.dto.Mod;
import com.faforever.api.dto.ModVersion;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.faforever.api.challonge.ChallongeController.CHALLONGE_READ_CACHE_NAME;
import static com.faforever.api.featuredmods.FeaturedModService.FEATURED_MOD_FILES_CACHE_NAME;
import static com.faforever.api.leaderboard.LeaderboardService.LEADERBOARD_GLOBAL_CACHE_NAME;
import static com.faforever.api.leaderboard.LeaderboardService.LEADERBOARD_RANKED_1V1_CACHE_NAME;
import static com.faforever.api.security.OAuthClientDetailsService.CLIENTS_CACHE_NAME;
import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@EnableCaching(proxyTargetClass = true)
@Configuration
@Profile(ApplicationProfile.PRODUCTION)
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Arrays.asList(
      // Elide entity caches
      new CaffeineCache(ElideConfig.DEFAULT_CACHE_NAME, newBuilder().maximumSize(0).build()),
      new CaffeineCache(Avatar.TYPE, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(AvatarAssignment.TYPE, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(Achievement.TYPE, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(Clan.TYPE, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(Event.TYPE, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(FeaturedMod.TYPE, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(Map.TYPE, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(MapVersion.TYPE, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(MapStatistics.TYPE, newBuilder().expireAfterWrite(1, MINUTES).build()),
      new CaffeineCache(Mod.TYPE, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(ModVersion.TYPE, newBuilder().expireAfterWrite(60, MINUTES).build()),
      // Other caches
      new CaffeineCache(CHALLONGE_READ_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(LEADERBOARD_RANKED_1V1_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(LEADERBOARD_GLOBAL_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(FEATURED_MOD_FILES_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(CLIENTS_CACHE_NAME, newBuilder().expireAfterWrite(5, SECONDS).build())
    ));
    return cacheManager;
  }

  /**
   * Returns a cache resolver that resolves cache names by JSON API type names. For instance, the type "map" will be
   * resolved to a cache named "map". If no dedicated cache config is available, the "default" config will be applied.
   */
  @Bean
  public CacheResolver elideCacheResolver(CacheManager cacheManager) {
    return new AbstractCacheResolver(cacheManager) {
      @Override
      protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        String entity = getEntity((HttpServletRequest) context.getArgs()[1]);

        if (!cacheManager.getCacheNames().contains(entity)) {
          return Collections.singletonList(ElideConfig.DEFAULT_CACHE_NAME);
        }

        return Collections.singletonList(entity);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private String getEntity(HttpServletRequest request) {
    return (String) ((java.util.Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get("entity");
  }
}
