package com.faforever.api.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class UserSupplier implements Supplier<FafAuthenticationToken> {

  @Override
  public FafAuthenticationToken get() {
    Object principal = SecurityContextHolder.getContext().getAuthentication();
    if (principal instanceof FafAuthenticationToken fafAuthenticationToken) {
      return fafAuthenticationToken;
    } else {
      return new FafAuthenticationToken(-1, List.of(), List.of());
    }
  }
}
