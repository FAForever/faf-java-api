package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Mod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

@Component
@Slf4j
public class ModChangeListener {

  private static EntityCacheEvictor cacheEvictor;

  @Inject
  public void init(EntityCacheEvictor cacheEvictor) {
    ModChangeListener.cacheEvictor = cacheEvictor;
  }

  @PostUpdate
  @PostRemove
  public void modChanged(Mod mod) {
    log.debug("Mod and ModVersion cache evicted, due to change on Mod with id: {}", mod.getId());
    cacheEvictor.evictModAndModVersionCaches();
  }
}
