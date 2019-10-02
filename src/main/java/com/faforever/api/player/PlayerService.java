package com.faforever.api.player;

import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.FafUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static com.faforever.api.error.ErrorCode.TOKEN_INVALID;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository playerRepository;

  public Player getPlayer(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() != null
        && authentication.getPrincipal() instanceof FafUserDetails) {
      return playerRepository.findById(((FafUserDetails) authentication.getPrincipal()).getId())
        .orElseThrow(() -> new ApiException(new Error(TOKEN_INVALID)));
    }
    throw new ApiException(new Error(TOKEN_INVALID));
  }

  public Player getById(Integer playerId) {
    return playerRepository.findById(playerId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, playerId)));
  }
}
