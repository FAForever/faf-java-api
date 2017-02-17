package com.faforever.api.security;

import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Collection;

import static java.util.Collections.singletonList;

@Getter
public class FafUserDetails extends org.springframework.security.core.userdetails.User {

  private final int id;

  public FafUserDetails(User user) {
    // TODO implement lobby_admin
    this(user.getId(), user.getLogin(), user.getPassword(), isNonLocked(user.getBanInfo()), singletonList(new SimpleGrantedAuthority("ROLE_USER")));
  }

  public FafUserDetails(int id, String username, String password, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
    super(username, password, true, true, true, accountNonLocked, authorities);
    this.id = id;
  }

  private static boolean isNonLocked(BanInfo banInfo) {
    return banInfo == null
        || banInfo.getExpiresAt().isBefore(OffsetDateTime.now());
  }
}
