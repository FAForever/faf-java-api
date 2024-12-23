package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClanMembershipChangeListener {

  @CacheEvict(cacheNames = Clan.TYPE_NAME, allEntries = true)
  @PostUpdate
  @PostRemove
  @PostPersist
  public void clanMembershipChanged(ClanMembership clanMembership) {
    log.debug("Clan cache evicted, due to change on ClanMembership with id: {}",
      clanMembership.getId());
  }
}
