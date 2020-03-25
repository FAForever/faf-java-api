package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Map;
import com.faforever.api.dto.MapVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

@Component
@Slf4j
public class MapChangeListener {

  @CacheEvict(allEntries = true, cacheNames = {com.faforever.api.dto.Map.TYPE, MapVersion.TYPE})
  @PostUpdate
  @PostRemove
  public void mapChanged(Map map) {
    log.debug("Map and MapVersion cache evicted, due to change on MapVersion with id: {}", map.getId());
  }
}
