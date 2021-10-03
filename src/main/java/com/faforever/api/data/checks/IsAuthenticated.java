package com.faforever.api.data.checks;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;
import com.yahoo.elide.core.security.checks.UserCheck;

public class IsAuthenticated {

  public static final String EXPRESSION = "is authenticated";

  @SecurityCheck(EXPRESSION)
  public static class Inline extends UserCheck {
    @Override
    public boolean ok(User user) {
      return user != null && user.getPrincipal() != null;
    }
  }
}
