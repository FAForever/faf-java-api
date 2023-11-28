package com.faforever.api.security;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Getter
public final class FafUserAuthenticationToken extends FafAuthenticationToken {

  private final int userId;
  private final String username;

  public FafUserAuthenticationToken(
    int userId,
    String username,
    @NotNull Collection<FafScope> scopes,
    @NotNull Collection<FafRole> roles
  ) {
    super(
      scopes,
      ImmutableList.<FafRole>builder()
        .addAll(roles)
        .add(new FafRole("USER"))
        .build()
    );
    this.userId = userId;
    this.username = username;
  }

  @Override
  public Object getPrincipal() {
    return userId;
  }

  @Override
  public String getName() {
    return String.format("User %s", userId);
  }
}
