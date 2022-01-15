package com.faforever.api.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

public record FafRole(@NotNull String role) implements GrantedAuthority {
  public static final String ROLE_PREFIX = "ROLE_";

  @Override
  public String getAuthority() {
    return ROLE_PREFIX + role;
  }

  public boolean matches(String matchingRole) {
    return Objects.equals(role, matchingRole);
  }
}
