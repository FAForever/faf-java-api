package com.faforever.api.player;

import com.faforever.api.data.domain.Player;
import com.faforever.api.security.FafUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class PlayerService {

  private final PlayerRepository playerRepository;

  @Inject
  public PlayerService(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  public Player getPlayer(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() != null
        && authentication.getPrincipal() instanceof FafUserDetails) {
      return playerRepository.findOne(((FafUserDetails) authentication.getPrincipal()).getId());
    }
    throw new IllegalStateException("Authentication missing");
  }
}
