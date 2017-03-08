package com.faforever.api.security;

import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserRepository;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.util.Collection;

import static java.util.Collections.singletonList;

@Getter
public class FafUserDetails extends org.springframework.security.core.userdetails.User {

  private final int id;
  private final UserRepository userRepository;
  private User user;

  public FafUserDetails(User user) {
    // TODO implement lobby_admin
    this(user.getId(),
        user.getLogin(),
        user.getPassword(),
        isNonLocked(user.getBanInfo()),
        singletonList(new SimpleGrantedAuthority("ROLE_USER")),
        null);
    this.user = user;
  }

  public FafUserDetails(int id, String username, String password,
                        boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
                        UserRepository userRepository) {
    super(username, password, true, true, true, accountNonLocked, authorities);
    this.id = id;
    this.userRepository = userRepository;
  }

  private static boolean isNonLocked(BanInfo banInfo) {
    return banInfo == null
        || banInfo.getExpiresAt().isBefore(OffsetDateTime.now());
  }

  public boolean hasPermission(String permission) {
    if (this.user == null) {
      user = userRepository.findOneByLoginIgnoreCase(getUsername());
    }
    if (user == null) {
      return false;
    }
    return user.hasPermission(permission);
  }
}
