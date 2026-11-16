package com.faforever.api.data.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GameDesyncTest {

  @Test
  void reportFileName() {
    Game game = new Game();
    game.setId(1234);
    Player player = new Player();
    player.setId(42);
    GameDesync gameDesync = new GameDesync();
    gameDesync.setGame(game);
    gameDesync.setPlayer(player);

    assertThat(gameDesync.getReportFileName(), is("desync/1234/42.zip"));
  }
}
