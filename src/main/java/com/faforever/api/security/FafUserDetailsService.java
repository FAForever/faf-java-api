package com.faforever.api.security;

import com.faforever.api.data.domain.LegacyAccessLevel;
import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Adapter between Spring's {@link UserDetailsService} and FAF's {@code login} table.
 */
@Service
@RequiredArgsConstructor
public class FafUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    User user = userRepository.findOneByLoginOrEmail(usernameOrEmail, usernameOrEmail)
      .orElseThrow(() -> new UsernameNotFoundException("User could not be found: " + usernameOrEmail));

    ArrayList<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(LegacyAccessLevel.ROLE_USER);

    if (user.getLobbyGroup() != null) {
      authorities.add(user.getLobbyGroup().getAccessLevel());
    }
    return new FafUserDetails(user, authorities);
  }
}
