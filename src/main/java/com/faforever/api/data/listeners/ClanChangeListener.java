package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Clan;

import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClanChangeListener {

  @CacheEvict(cacheNames = Clan.TYPE_NAME, allEntries = true)
  @PostUpdate
  @PostRemove
  public void clanChanged(Clan clan) {
    log.debug("Clan cache evicted, due to change on Clan with id: {}", clan.getId());
  }
}
