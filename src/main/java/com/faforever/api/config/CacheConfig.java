package com.faforever.api.config;

import com.faforever.api.config.elide.ElideConfig;
import com.faforever.api.data.domain.Achievement;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.AvatarAssignment;
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
import com.faforever.api.data.domain.MapStatistics;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.faforever.api.challonge.ChallongeController.CHALLONGE_READ_CACHE_NAME;
import static com.faforever.api.featuredmods.FeaturedModService.FEATURED_MOD_FILES_CACHE_NAME;
import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;

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
      new CaffeineCache(Avatar.TYPE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(AvatarAssignment.TYPE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(Achievement.TYPE_NAME, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(Clan.TYPE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(CoopResult.TYPE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(CoopScenario.TYPE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(CoopMap.TYPE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(Event.TYPE_NAME, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(FeaturedMod.TYPE_NAME, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(Game.TYPE_NAME, newBuilder().expireAfterWrite(1, MINUTES).build()),
      new CaffeineCache(Map.TYPE_NAME, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(MapVersion.TYPE_NAME, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(MapStatistics.TYPE_NAME, newBuilder().expireAfterWrite(1, MINUTES).build()),
      new CaffeineCache(Mod.TYPE_NAME, newBuilder().expireAfterWrite(60, MINUTES).build()),
      new CaffeineCache(ModVersion.TYPE_NAME, newBuilder().expireAfterWrite(60, MINUTES).build()),
      // Other caches
      new CaffeineCache(CHALLONGE_READ_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(FEATURED_MOD_FILES_CACHE_NAME, newBuilder().expireAfterWrite(5, MINUTES).build()),
      new CaffeineCache(Leaderboard.TYPE_NAME, newBuilder().expireAfterWrite(1, MINUTES).build()),
      new CaffeineCache(LeaderboardRating.TYPE_NAME, newBuilder().expireAfterWrite(1, MINUTES).build())
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
