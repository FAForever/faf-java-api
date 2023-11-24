package com.faforever.api.security;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Getter
public final class FafServiceAuthenticationToken extends FafAuthenticationToken {

  private final String serviceName;

  public FafServiceAuthenticationToken(
    String serviceName,
    @NotNull Collection<FafScope> scopes
  ) {
    super(
      scopes,
      ImmutableList.<FafRole>builder()
        .add(new FafRole("SERVICE"))
        .build()
    );
    this.serviceName = serviceName;
  }

  @Override
  public Object getPrincipal() {
    return serviceName;
  }

  @Override
  public String getName() {
    return String.format("Service %s", serviceName);
  }
}
