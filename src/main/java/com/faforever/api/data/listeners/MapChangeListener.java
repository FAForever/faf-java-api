package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Map;
import com.faforever.api.data.domain.MapVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

@Component
@Slf4j
public class MapChangeListener {

  @CacheEvict(allEntries = true, cacheNames = {Map.TYPE_NAME, MapVersion.TYPE_NAME})
  @PostUpdate
  @PostRemove
  public void mapChanged(Map map) {
    log.debug("Map and MapVersion cache evicted, due to change on Map with id: {}", map.getId());
  }
}
