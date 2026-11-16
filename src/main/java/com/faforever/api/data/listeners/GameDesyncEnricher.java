package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.GameDesync;
import com.faforever.api.desync.DesyncReportService;
import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;
import org.springframework.stereotype.Component;

@Component
public class GameDesyncEnricher {

  private static DesyncReportService desyncReportService;

  @Inject
  public void init(DesyncReportService desyncReportService) {
    GameDesyncEnricher.desyncReportService = desyncReportService;
  }

  @PostLoad
  public void enrich(GameDesync gameDesync) {
    gameDesync.setReportFileName(desyncReportService.getReportFileName(gameDesync.getGame().getId(), gameDesync.getPlayer().getId()));
  }
}
