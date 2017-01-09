package com.faforever.api.config.elide.checks;

import com.yahoo.elide.security.User;
import com.yahoo.elide.security.checks.UserCheck;

/**
 * @author Dragonfire
 */
public class IsUser extends UserCheck {
  @Override
  public boolean ok(User user) {
    return user != null & user.getOpaqueUser() != null;
  }
}
