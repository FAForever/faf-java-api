package com.faforever.api.utils;

import java.util.Objects;
import java.util.regex.Pattern;

// TODO move to shared FAF code
final class Validator {

  private static final Pattern INT_PATTERN = Pattern.compile("\\d+");

  private Validator() {
    // Utility class
  }

  /**
   * Throws a NullPointerException with the specified message when {@code object} is null.
   *
   * @param object the object to check for null
   * @param message the exception message
   */
  static void notNull(Object object, String message) {
    Objects.requireNonNull(object);
  }

  public static boolean isInt(String string) {
    return INT_PATTERN.matcher(string).matches();
  }

}
