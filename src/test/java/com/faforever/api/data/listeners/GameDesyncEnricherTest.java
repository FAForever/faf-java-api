package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Game;
import com.faforever.api.data.domain.GameDesync;
import com.faforever.api.data.domain.Player;
import com.faforever.api.desync.DesyncReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameDesyncEnricherTest {

  @Mock
  private DesyncReportService desyncReportService;

  @Test
  void enrichSetsReportFileName() {
    Game game = new Game();
    game.setId(1234);
    Player player = new Player();
    player.setId(42);
    GameDesync gameDesync = new GameDesync();
    gameDesync.setGame(game);
    gameDesync.setPlayer(player);

    when(desyncReportService.getReportFileName(1234, 42)).thenReturn("desync/1234/42.zip");
    GameDesyncEnricher enricher = new GameDesyncEnricher();
    enricher.init(desyncReportService);

    enricher.enrich(gameDesync);

    assertThat(gameDesync.getReportFileName(), is("desync/1234/42.zip"));
  }
}
