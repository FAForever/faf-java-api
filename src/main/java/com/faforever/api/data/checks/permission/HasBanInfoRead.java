package com.faforever.api.data.checks.permission;

public class HasBanInfoRead {
  public static final String EXPRESSION = "Ban.Read";

  public static class Inline extends BasePermission {
    public Inline() {
      super(HasBanInfoRead.EXPRESSION);
    }
  }
}
