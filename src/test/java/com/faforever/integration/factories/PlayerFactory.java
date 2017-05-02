package com.faforever.integration.factories;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.User;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.user.UserRepository;

public class PlayerFactory {
  public static Player createPlayer(String login, UserRepository userRepository, PlayerRepository playerRepository) throws Exception {
    User user = (User) new User()
        .setPassword("foo")
        .setLogin(login)
        .setEmail(login + "@faforever.com");
    userRepository.save(user);
    return playerRepository.findOne(user.getId());
  }
}
