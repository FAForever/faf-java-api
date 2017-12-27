package com.faforever.api.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.function.Supplier;

@Component
public class UserSupplier implements Supplier<FafUserDetails> {

  @Override
  public FafUserDetails get() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof FafUserDetails) {
      return (FafUserDetails) principal;
    } else {
      return new FafUserDetails(-1, principal.toString(), null, false, Collections.emptyList());
    }
  }
}
