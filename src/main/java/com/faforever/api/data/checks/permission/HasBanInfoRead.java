package com.faforever.api.data.checks.permission;

import com.faforever.api.security.FafUserDetails;
import com.yahoo.elide.security.User;
import com.yahoo.elide.security.checks.UserCheck;

public class HasBanInfoRead {
  public static final String EXPRESSION = "Ban.Read";

  public static class Inline extends UserCheck {
    @Override
    public boolean ok(User user) {
      return user.getOpaqueUser() instanceof FafUserDetails &&
          ((FafUserDetails) user.getOpaqueUser()).hasPermission(HasBanInfoRead.EXPRESSION);
    }
  }
}
