package com.faforever.api.data.checks.permission;

import com.faforever.api.security.FafUserDetails;
import com.yahoo.elide.security.User;
import com.yahoo.elide.security.checks.UserCheck;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BasePermission extends UserCheck {

  private final String permission;

  @Override
  public boolean ok(User user) {
    return user.getOpaqueUser() instanceof FafUserDetails &&
        ((FafUserDetails) user.getOpaqueUser()).hasPermission(permission);
  }
}
