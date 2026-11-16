package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.GameDesync;
import com.faforever.api.desync.DesyncReportService;
import jakarta.persistence.PostLoad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameDesyncEnricher {

  private final DesyncReportService desyncReportService;

  @PostLoad
  public void enrich(GameDesync gameDesync) {
    gameDesync.setReportFileName(desyncReportService.getReportFileName(gameDesync.getGame().getId(), gameDesync.getPlayer().getId()));
  }
}
