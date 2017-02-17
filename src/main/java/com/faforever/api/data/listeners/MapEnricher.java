package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Map;

import javax.persistence.PostLoad;

public class MapEnricher {

  @PostLoad
  public void enhance(Map map) {
    if (map.getLatestVersion() != null) {
//      map.setUpdateTime(map.getLatestVersion().getUpdateTime());
    }
  }
}
