package com.faforever.api.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum UserRole implements GrantedAuthority {
  USER("USER"),
  MODERATOR("MODERATOR"),
  ADMINISTRATOR("ADMINISTRATOR");

  private static final Map<String, UserRole> fromString;

  static {
    fromString = new HashMap<>();
    for (UserRole userRole : values()) {
      fromString.put(userRole.string, userRole);
    }
  }

  private final String string;

  UserRole(String string) {
    this.string = string;
  }

  public static Optional<UserRole> fromString(String string) {
    return Optional.ofNullable(fromString.get(string));
  }

  @Override
  public String getAuthority() {
    return "ROLE_" + string;
  }
}
