package com.faforever.api.authentication;

import com.faforever.api.data.domain.Player;
import com.faforever.api.user.FafUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

  public Player getPlayer(org.springframework.security.core.Authentication authentication,
                                 JpaRepository<Player, Integer> playerRepo) {
    if (authentication != null
        && authentication.getPrincipal() != null
        && authentication.getPrincipal() instanceof FafUserDetails) {
      return playerRepo.findOne(((FafUserDetails) authentication.getPrincipal()).getId());
    }
    throw new IllegalStateException("Authentication missing");
  }
}
