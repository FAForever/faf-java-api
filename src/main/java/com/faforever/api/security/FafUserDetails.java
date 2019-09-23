package com.faforever.api.security;

import com.faforever.api.data.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class FafUserDetails extends org.springframework.security.core.userdetails.User {

  private final int id;

  public FafUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
    this(user.getId(), user.getLogin(), user.getPassword(), !user.isGlobalBanned(), authorities);
  }

  public FafUserDetails(int id, String username, String password, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
    super(username, password, true, true, true, accountNonLocked, authorities);
    this.id = id;
  }

  public boolean hasPermission(String permission) {
    Collection<String> authorities = this.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toList());

    return authorities.contains(permission) || authorities.contains(UserRole.ADMINISTRATOR.getAuthority());
  }
}
