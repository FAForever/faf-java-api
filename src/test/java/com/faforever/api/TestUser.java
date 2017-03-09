package com.faforever.api;

import com.faforever.api.permission.PermissionService;
import com.faforever.api.security.FafUserDetails;

import java.util.Collections;

public class TestUser {

  private TestUser() {

  }

  public static FafUserDetails testUser(PermissionService permissionService) {
    return new FafUserDetails(1, "junit", "n/a", true, Collections.emptyList(), permissionService);
  }
}
