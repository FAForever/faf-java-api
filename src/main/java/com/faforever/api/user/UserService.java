package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import com.faforever.api.security.FafUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class UserService {

  private final UserRepository userRepository;

  @Inject
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getPlayer(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() != null
        && authentication.getPrincipal() instanceof FafUserDetails) {
      return userRepository.findOne(((FafUserDetails) authentication.getPrincipal()).getId());
    }
    throw new IllegalStateException("Authentication missing");
  }
}
