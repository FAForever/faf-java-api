package com.faforever.api.security;

import com.faforever.api.data.domain.LegacyAccessLevel;
import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;

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
    User user = userRepository.findOneByLoginIgnoreCase(username)
      .orElseThrow(() -> new UsernameNotFoundException("User could not be found: " + username));

    ArrayList<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(LegacyAccessLevel.ROLE_USER);

    if (user.getLobbyGroup() != null) {
      authorities.add(LegacyAccessLevel.fromCode(user.getLobbyGroup().getAccessLevel()));
    }
    return new FafUserDetails(user, authorities);
  }
}
