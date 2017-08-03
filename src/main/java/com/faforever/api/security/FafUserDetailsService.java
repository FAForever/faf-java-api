package com.faforever.api.security;

import com.faforever.api.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Adapter between Spring's {@link UserDetailsService} and FAF's {@code login} table.
 */
@Service
public class FafUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Inject
  public FafUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    if (userRepository.findOneByLoginIgnoreCase(username) == null) {
      throw new UsernameNotFoundException();
    }
    return new FafUserDetails(userRepository.findOneByLoginIgnoreCase(username));
  }
}
