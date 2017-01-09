package com.faforever.api.utils;

import com.faforever.api.data.domain.Player;
import com.faforever.api.user.FafUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public class AuthenticationHelper {

  public static Player getPlayer(org.springframework.security.core.Authentication auth,
                                           JpaRepository<Player, Integer> playerRepo) {
    if (auth != null && auth.getPrincipal() != null && auth.getPrincipal() instanceof FafUserDetails) {
      return playerRepo.findOne(((FafUserDetails) auth.getPrincipal()).getId());
    }
    throw new IllegalStateException("Authentication missing");
  }
}
