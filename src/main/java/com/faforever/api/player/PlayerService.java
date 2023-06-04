package com.faforever.api.player;

import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.FafAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PlayerService {

  private final PlayerRepository playerRepository;

  public Player getCurrentPlayer() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication instanceof FafAuthenticationToken fafAuthenticationToken) {
      return playerRepository.findById((fafAuthenticationToken.getUserId()))
        .orElseThrow(() -> ApiException.of(ErrorCode.TOKEN_INVALID));
    }
    throw ApiException.of(ErrorCode.TOKEN_INVALID);
  }

  public Player getById(Integer playerId) {
    return playerRepository.findById(playerId)
      .orElseThrow(() -> ApiException.of(ErrorCode.PLAYER_NOT_FOUND, playerId));
  }
}
