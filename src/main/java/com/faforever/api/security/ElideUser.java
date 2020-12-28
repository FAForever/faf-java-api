package com.faforever.api.security;

import com.yahoo.elide.core.security.User;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.Optional;

public class ElideUser extends User {
  protected FafUserDetails fafUserDetails;

  public ElideUser(Principal principal) {
    super(principal);
    if (principal instanceof Authentication) {
      this.fafUserDetails = (FafUserDetails) ((Authentication) principal).getPrincipal();
    }
  }

  @Override
  public String getName() {
    return getFafUserDetails().map(details -> details.getUsername()).orElse("");
  }

  @Override
  public boolean isInRole(String role) {
    return getFafUserDetails().map(details -> details.hasPermission(role)).orElse(false);
  }

  public Optional<FafUserDetails> getFafUserDetails() {
    return Optional.ofNullable(fafUserDetails);
  }
}
