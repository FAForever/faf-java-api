package com.faforever.api.player;

import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.security.FafUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.faforever.api.error.ErrorCode.TOKEN_INVALID;

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
    throw new ApiException(new Error(TOKEN_INVALID));
  }
}
