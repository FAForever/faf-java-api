package com.faforever.api.utils;

import com.faforever.api.data.domain.Player;
import com.faforever.api.user.FafUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public class AuthenticationHelper {

  public static Player getPlayer(org.springframework.security.core.Authentication authentication,
                                    JpaRepository<Player, Integer> playerRepo) {
    if (authentication != null
        && authentication.getPrincipal() != null
        && authentication.getPrincipal() instanceof FafUserDetails) {
      return playerRepo.findOne(((FafUserDetails) authentication.getPrincipal()).getId());
    }
    throw new IllegalStateException("Authentication missing");
  }
}
