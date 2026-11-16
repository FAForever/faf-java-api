package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Game;
import com.faforever.api.data.domain.GameDesync;
import com.faforever.api.data.domain.Player;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GameDesyncEnricherTest {

  @Test
  void enrichSetsReportFileName() {
    Game game = new Game();
    game.setId(1234);
    Player player = new Player();
    player.setId(42);
    GameDesync gameDesync = new GameDesync();
    gameDesync.setGame(game);
    gameDesync.setPlayer(player);

    new GameDesyncEnricher().enrich(gameDesync);

    assertThat(gameDesync.getReportFileName(), is("desync/1234/42.zip"));
  }
}
