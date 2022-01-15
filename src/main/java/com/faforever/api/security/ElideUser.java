package com.faforever.api.security;

import com.yahoo.elide.core.security.User;
import lombok.Getter;

import java.security.Principal;
import java.util.Optional;

public class ElideUser extends User {
  @Getter
  protected FafAuthenticationToken fafAuthentication;

  public ElideUser(Principal principal) {
    super(principal);
    if (principal instanceof FafAuthenticationToken authentication) {
      this.fafAuthentication = authentication;
    }
  }

  @Override
  public String getName() {
    return fafAuthentication.getName();
  }

  @Override
  public boolean isInRole(String role) {
    return fafAuthentication.hasRole(role);
  }

  public Optional<Integer> getFafId() {
    return Optional.ofNullable(fafAuthentication).map(FafAuthenticationToken::getUserId);
  }
}
