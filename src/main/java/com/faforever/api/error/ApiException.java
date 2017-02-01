package com.faforever.api.error;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ApiException extends RuntimeException {

  private final Error[] errors;

  public ApiException(Error error, Object... args) {
    this.errors = new Error[]{error};
  }

  public ApiException(Error[] errors, Object... args) {
    this.errors = errors;
  }
}
