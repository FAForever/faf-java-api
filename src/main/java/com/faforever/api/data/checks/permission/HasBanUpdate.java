package com.faforever.api.data.checks.permission;

public class HasBanUpdate {
  public static final String EXPRESSION = "Ban.Update";

  public static class Inline extends BasePermission {
    public Inline() {
      super(HasBanUpdate.EXPRESSION);
    }
  }
}
