package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.ClanMembership;

import jakarta.inject.Inject;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClanMembershipChangeListener {

  private static EntityCacheEvictor cacheEvictor;

  @Inject
  public void init(EntityCacheEvictor cacheEvictor) {
    ClanMembershipChangeListener.cacheEvictor = cacheEvictor;
  }

  @PostUpdate
  @PostRemove
  @PostPersist
  public void clanMembershipChanged(ClanMembership clanMembership) {
    log.debug("Clan cache evicted, due to change on ClanMembership with id: {}",
      clanMembership.getId());
    cacheEvictor.evictClanCache();
  }
}
