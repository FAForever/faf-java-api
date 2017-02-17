package com.faforever.api.data.checks;

import com.yahoo.elide.security.User;
import com.yahoo.elide.security.checks.UserCheck;

public class IsAuthenticated {

  public static final String EXPRESSION = "is authenticated";

  public static class Inline extends UserCheck {
    @Override
    public boolean ok(User user) {
      return user != null & user.getOpaqueUser() != null;
    }
  }
}
