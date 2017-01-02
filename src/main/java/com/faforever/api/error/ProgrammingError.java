package com.faforever.api.error;

/**
 * Exception to be thrown whenever something happened that's not supposed to happen unless there is a programming error.
 */
public class ProgrammingError extends RuntimeException {

  public ProgrammingError(String message) {
    super(message);
  }
}
