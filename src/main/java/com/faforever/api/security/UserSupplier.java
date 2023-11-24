package com.faforever.api.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSupplier {

  public Optional<FafAuthenticationToken> get() {
    Object principal = SecurityContextHolder.getContext().getAuthentication();
    if (principal instanceof FafAuthenticationToken fafAuthenticationToken) {
      return Optional.of(fafAuthenticationToken);
    } else {
      return Optional.empty();
    }
  }
}
