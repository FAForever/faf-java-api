package com.faforever.api.security;

import com.faforever.api.data.domain.BanDetails;
import com.faforever.api.data.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;

import static java.util.Collections.singletonList;

@Getter
public class FafUserDetails extends org.springframework.security.core.userdetails.User {

  private final int id;

  public FafUserDetails(User user) {
    // TODO implement lobby_admin
    this(user.getId(), user.getLogin(), user.getPassword(), isNonLocked(user.getBanDetails()), singletonList(new SimpleGrantedAuthority("ROLE_USER")));
  }

  public FafUserDetails(int id, String username, String password, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
    super(username, password, true, true, true, accountNonLocked, authorities);
    this.id = id;
  }

  private static boolean isNonLocked(BanDetails banDetails) {
    return banDetails == null
        || banDetails.getExpiresAt().before(Timestamp.from(Instant.now()));
  }
}
