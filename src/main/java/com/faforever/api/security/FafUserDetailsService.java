package com.faforever.api.security;

import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter between Spring's {@link UserDetailsService} and FAF's {@code login} table.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FafUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    User user = userRepository.findOneByLoginOrEmail(usernameOrEmail, usernameOrEmail)
      .orElseThrow(() -> new UsernameNotFoundException("User could not be found: " + usernameOrEmail));

    ArrayList<GrantedAuthority> authorities = new ArrayList<>();

    authorities.addAll(getUserRoles(user));
    authorities.addAll(getPermissionRoles(user));

    return new FafUserDetails(user, authorities);
  }

  private Collection<GrantedAuthority> getUserRoles(User user) {
    Set<GrantedAuthority> userRoles = new HashSet<>();

    userRoles.add(UserRole.USER);

    user.getUserGroups().stream()
      .map(userGroup -> UserRole.fromString(userGroup.getTechnicalName()))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .forEach(userRoles::add);

    return userRoles;
  }

  private Collection<GrantedAuthority> getPermissionRoles(User user) {
    return user.getUserGroups().stream()
      .flatMap(userGroup -> userGroup.getPermissions().stream())
      .collect(Collectors.toSet());
  }
}
