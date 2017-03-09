package com.faforever.api.data.checks.permission;

public class HasBanInfoCreate {
  public static final String EXPRESSION = "Ban.Create";

  public static class Inline extends BasePermission {
    public Inline() {
      super(HasBanInfoCreate.EXPRESSION);
    }
  }
}
