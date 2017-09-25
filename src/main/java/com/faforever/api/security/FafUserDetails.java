package com.faforever.api.security;

import com.faforever.api.data.domain.LegacyAccessLevel;
import com.faforever.api.data.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

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
    Collection<GrantedAuthority> authorities = this.getAuthorities();

    if (authorities.contains(LegacyAccessLevel.ROLE_ADMINISTRATOR)) {
      return true;
    }

    if (authorities.contains(LegacyAccessLevel.ROLE_MODERATOR)) {
      // We have no admin-only permissions yet
      return true;
    }

    return false;
  }
}
