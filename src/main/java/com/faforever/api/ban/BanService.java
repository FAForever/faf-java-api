package com.faforever.api.ban;

import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import org.springframework.stereotype.Service;

@Service
public class BanService {
  private final PlayerRepository playerRepository;

  public BanService(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  public boolean hasActiveGlobalBan(Player player) {
    return player.isGlobalBanned();
  }

  public boolean hasActiveGlobalBan(String username) {
    return hasActiveGlobalBan(playerRepository.findOneByLoginIgnoreCase(username));
  }
}
