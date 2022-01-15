package com.faforever.api.security;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class FafAuthenticationToken extends AbstractAuthenticationToken {

  private final int userId;
  private final Collection<FafScope> scopes;
  private final Collection<FafRole> roles;

  public FafAuthenticationToken(
    int userId,
    @NotNull Collection<FafScope> scopes,
    @NotNull Collection<FafRole> roles
  ) {
    super(
      ImmutableList.<GrantedAuthority>builder()
        .addAll(scopes)
        .addAll(roles)
        // ROLE_USER is an implicit role by Spring Security usually set during regular authentication, so we add it too
        .add((GrantedAuthority) () -> "ROLE_USER")
        .build()
    );
    this.userId = userId;
    this.scopes = scopes;
    this.roles = roles;
    // since the access token was already verified, each FafAuthenticationToken is implicitly authenticated
    this.setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return userId;
  }

  @Override
  public String getName() {
    return String.format("User %s", userId);
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
