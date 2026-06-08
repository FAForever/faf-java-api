package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Map;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModVersion;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

// Holds the @CacheEvict methods that JPA entity listeners delegate to.
// Listeners themselves must not carry Spring-AOP-triggering annotations, because
// Hibernate's ManagedBeanRegistry strictly checks the listener bean's class
// against the type registered via @EntityListeners; a CGLIB proxy fails that check.
@Component
public class EntityCacheEvictor {

  @CacheEvict(allEntries = true, cacheNames = {Map.TYPE_NAME, MapVersion.TYPE_NAME})
  public void evictMapAndMapVersionCaches() {
  }

  @CacheEvict(allEntries = true, cacheNames = {Mod.TYPE_NAME, ModVersion.TYPE_NAME})
  public void evictModAndModVersionCaches() {
  }

  @CacheEvict(cacheNames = Clan.TYPE_NAME, allEntries = true)
  public void evictClanCache() {
  }
}
