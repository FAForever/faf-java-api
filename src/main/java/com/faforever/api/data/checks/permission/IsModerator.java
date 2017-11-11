package com.faforever.api.data.checks.permission;

public class IsModerator {
  public static final String EXPRESSION = "IsModerator";

  public static class Inline extends BasePermission {
    public Inline() {
      super(IsModerator.EXPRESSION);
    }
  }
}
