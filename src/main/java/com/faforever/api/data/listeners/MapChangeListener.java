package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

@Component
@Slf4j
public class MapChangeListener {

  private static EntityCacheEvictor cacheEvictor;

  @Inject
  public void init(EntityCacheEvictor cacheEvictor) {
    MapChangeListener.cacheEvictor = cacheEvictor;
  }

  @PostUpdate
  @PostRemove
  public void mapChanged(Map map) {
    log.debug("Map and MapVersion cache evicted, due to change on Map with id: {}", map.getId());
    cacheEvictor.evictMapAndMapVersionCaches();
  }
}
