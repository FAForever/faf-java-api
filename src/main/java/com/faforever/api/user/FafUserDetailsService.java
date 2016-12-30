package com.faforever.api.user;

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
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return new FafUserDetails(userRepository.findOneByLogin(username));
  }
}
