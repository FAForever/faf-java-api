package com.faforever.integration.factories;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.User;
import com.faforever.integration.TestDatabase;
import lombok.SneakyThrows;

public class PlayerFactory {
  @SneakyThrows
  public static Player createPlayer(String login, TestDatabase database) {
    User user = (User) new User()
      .setPassword("foo")
      .setLogin(login)
      .setEmail(login + "@faforever.com");
    database.getUserRepository().save(user);
    return database.getPlayerRepository().findOne(user.getId());
  }
}
