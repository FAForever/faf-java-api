package com.faforever.api.security;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public abstract sealed class FafAuthenticationToken extends AbstractAuthenticationToken
  permits FafUserAuthenticationToken, FafServiceAuthenticationToken {

  protected final Collection<FafScope> scopes;
  protected final Collection<FafRole> roles;

  public FafAuthenticationToken(
    @NotNull Collection<FafScope> scopes,
    @NotNull Collection<FafRole> roles
  ) {
    super(
      ImmutableList.<GrantedAuthority>builder()
        .addAll(scopes)
        .addAll(roles)
        .build()
    );
    this.scopes = scopes;
    this.roles = roles;
    // since the access token was already verified, each FafAuthenticationToken is implicitly authenticated
    this.setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  public boolean hasRole(String role) {
    return roles.stream()
      .anyMatch(r -> r.matches(role));
  }

  public boolean hasScope(String scope) {
    return scopes.stream()
      .anyMatch(s -> s.matches(scope));
  }
}
