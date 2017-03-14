package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Game;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Component
public class GameEnricher {

  private static FafApiProperties fafApiProperties;

  @Inject
  public void init(FafApiProperties fafApiProperties) {
    GameEnricher.fafApiProperties = fafApiProperties;
  }

  @PostLoad
  public void enhance(Game game) {
    game.setReplayUrl(String.format(fafApiProperties.getReplay().getDownloadUrlFormat(), game.getId()));
  }
}
