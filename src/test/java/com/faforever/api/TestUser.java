package com.faforever.api;

import com.faforever.api.user.FafUserDetails;

import java.util.Collections;

public class TestUser {

  private TestUser() {

  }

  public static FafUserDetails testUser() {
    return new FafUserDetails(1, "junit", "n/a", true, Collections.emptyList());
  }
}
