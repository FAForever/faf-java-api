package com.faforever.api.security;

import com.faforever.api.data.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

@Getter
public class FafUserDetails extends org.springframework.security.core.userdetails.User {

  private final int id;
  /**
   * Workaround for compatibility with old Spring OAuth2 token AND new OpenID Connect token
   */
  private final Set<String> scopes;

  public FafUserDetails(User user, Collection<? extends GrantedAuthority> authorities, Set<String> scopes) {
    this(user.getId(), user.getLogin(), user.getPassword(), !user.isGlobalBanned(), authorities, scopes);
  }

  public FafUserDetails(int id, String username, String password, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, Set<String> scopes) {
    super(username, password, true, true, true, accountNonLocked, authorities);
    this.id = id;
    this.scopes = scopes;
  }

  public boolean hasPermission(String permission) {
    Collection<String> authorities = this.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .toList();

    return authorities.contains(permission) || authorities.contains(UserRole.ADMINISTRATOR.getAuthority());
  }
}
