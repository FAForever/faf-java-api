package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Game;
import com.faforever.api.game.GameService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

@Component
public class GameEnricher {

  private static GameService gameService;

  @Inject
  public void init(GameService gameService) {
    GameEnricher.gameService = gameService;
  }

  @PostLoad
  public void enrich(Game game) {
    game.setReplayUrl(gameService.getReplayDownloadUrl(game.getId()));
    game.setName(StringEscapeUtils.unescapeHtml4(game.getName()));
  }
}
