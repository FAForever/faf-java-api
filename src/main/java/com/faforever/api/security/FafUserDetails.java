package com.faforever.api.security;

import com.faforever.api.data.domain.User;
import com.faforever.api.permission.PermissionService;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static java.util.Collections.singletonList;

@Getter
public class FafUserDetails extends org.springframework.security.core.userdetails.User {

  private final int id;
  private final PermissionService permissionService;
  private User user;

  public FafUserDetails(User user) {
    // TODO implement lobby_admin
    this(user.getId(),
        user.getLogin(),
        user.getPassword(),
        true, // TODO use new ban system
        singletonList(new SimpleGrantedAuthority("ROLE_USER")),
        null);
    this.user = user;
  }

  public FafUserDetails(int id, String username, String password,
                        boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
                        PermissionService permissionService) {
    super(username, password, true, true, true, accountNonLocked, authorities);
    this.id = id;
    this.permissionService = permissionService;
  }

  public boolean hasPermission(String permission) {
    if (user != null) {
      return user.hasPermission(permission);
    }
    if (permissionService != null) {
      return permissionService.hasPermission(getUsername(), permission);
    }
    throw new IllegalStateException("permissionService and user cannot be both null");
  }
}
