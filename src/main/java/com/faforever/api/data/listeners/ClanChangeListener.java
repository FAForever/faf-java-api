package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Clan;

import jakarta.inject.Inject;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClanChangeListener {

  private static EntityCacheEvictor cacheEvictor;

  @Inject
  public void init(EntityCacheEvictor cacheEvictor) {
    ClanChangeListener.cacheEvictor = cacheEvictor;
  }

  @PostUpdate
  @PostRemove
  public void clanChanged(Clan clan) {
    log.debug("Clan cache evicted, due to change on Clan with id: {}", clan.getId());
    cacheEvictor.evictClanCache();
  }
}
