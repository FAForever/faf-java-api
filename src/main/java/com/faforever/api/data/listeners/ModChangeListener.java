package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

@Component
@Slf4j
public class ModChangeListener {

  @CacheEvict(allEntries = true, cacheNames = {Mod.TYPE_NAME, ModVersion.TYPE_NAME})
  @PostUpdate
  @PostRemove
  public void modChanged(Mod mod) {
    log.debug("Mod and ModVersion cache evicted, due to change on Mod with id: {}", mod.getId());
  }
}
