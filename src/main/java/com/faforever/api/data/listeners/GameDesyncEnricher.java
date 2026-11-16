package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.GameDesync;
import com.faforever.api.desync.DesyncReportService;
import jakarta.persistence.PostLoad;
import org.springframework.stereotype.Component;

@Component
public class GameDesyncEnricher {

  @PostLoad
  public void enrich(GameDesync gameDesync) {
    gameDesync.setReportFileName(DesyncReportService.getObjectKey(gameDesync.getGame().getId(), gameDesync.getPlayer().getId()));
  }
}
