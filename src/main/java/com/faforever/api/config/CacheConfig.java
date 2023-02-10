package com.faforever.api.config;

import com.faforever.api.config.elide.ElideConfig;
import com.faforever.api.data.domain.Achievement;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.CoopMap;
import com.faforever.api.data.domain.CoopResult;
import com.faforever.api.data.domain.CoopScenario;
import com.faforever.api.data.domain.Event;
import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.data.domain.Game;
import com.faforever.api.data.domain.Leaderboard;
import com.faforever.api.data.domain.LeaderboardRating;
import com.faforever.api.data.domain.Map;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModVersion;
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.faforever.api.challonge.ChallongeController.CHALLONGE_READ_CACHE_NAME;
import static com.faforever.api.featuredmods.FeaturedModService.FEATURED_MOD_FILES_CACHE_NAME;
import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;

@EnableCaching(proxyTargetClass = true)
@Configuration
@Profile(ApplicationProfile.PRODUCTION)
public class CacheConfig {

  /**
   * To be used when there is only one "main" request (e.g. show full list) and not many deviations exist
   */
  private static final long SMALL_SIZE = 25;
  /**
   * To be used when there is one "main" request we want to cache (e.g. show full list) but there are
   * deviating queries that might overwrite the main request.
   */
  private static final long MEDIUM_SIZE = 50;
  /**
   * To be used when many recurring calls expected (e.g. maps and mods in open lobbies)
   */
  private static final long BIG_SIZE = 100;

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(List.of(
      // Elide entity caches
      new CaffeineCache(ElideConfig.DEFAULT_CACHE_NAME, newBuilder().maximumSize(0).build()),
      buildCache(Avatar.TYPE_NAME, 5, MINUTES, SMALL_SIZE),
      buildCache(Achievement.TYPE_NAME, 60, MINUTES, SMALL_SIZE),
      buildCache(Clan.TYPE_NAME, 5, MINUTES, SMALL_SIZE),
      buildCache(CoopResult.TYPE_NAME, 5, MINUTES, MEDIUM_SIZE),
      buildCache(CoopScenario.TYPE_NAME, 5, MINUTES, SMALL_SIZE),
      buildCache(CoopMap.TYPE_NAME, 5, MINUTES, MEDIUM_SIZE),
      buildCache(Event.TYPE_NAME, 60, MINUTES, SMALL_SIZE),
      buildCache(FeaturedMod.TYPE_NAME, 60, MINUTES, SMALL_SIZE),
      buildCache(Game.TYPE_NAME, 2, MINUTES, BIG_SIZE),
      buildCache(Map.TYPE_NAME, 60, MINUTES, BIG_SIZE),
      buildCache(MapVersion.TYPE_NAME, 60, MINUTES, BIG_SIZE),
      buildCache(Mod.TYPE_NAME, 60, MINUTES, BIG_SIZE),
      buildCache(ModVersion.TYPE_NAME, 60, MINUTES, BIG_SIZE),

      // Other caches
      buildCache(CHALLONGE_READ_CACHE_NAME, 5, MINUTES, SMALL_SIZE),
      buildCache(FEATURED_MOD_FILES_CACHE_NAME, 5, MINUTES, MEDIUM_SIZE),
      buildCache(Leaderboard.TYPE_NAME, 2, MINUTES, SMALL_SIZE),
      buildCache(LeaderboardRating.TYPE_NAME, 2, MINUTES, SMALL_SIZE)
    ));
    return cacheManager;
  }

  private CaffeineCache buildCache(String key, long duration, TimeUnit unit, long maxSize) {
    return new CaffeineCache(
      key,
      newBuilder()
        .expireAfterWrite(duration, unit)
        .maximumSize(maxSize)
        .build()
    );
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
          return List.of(ElideConfig.DEFAULT_CACHE_NAME);
        }

        return List.of(entity);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private String getEntity(HttpServletRequest request) {
    return (String) ((java.util.Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get("entity");
  }
}
