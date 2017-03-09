package com.faforever.api.data.checks.permission;

public class HasBanRead {
  public static final String EXPRESSION = "Ban.Read";

  public static class Inline extends BasePermission {
    public Inline() {
      super(HasBanRead.EXPRESSION);
    }
  }
}
