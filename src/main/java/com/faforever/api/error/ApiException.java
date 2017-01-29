package com.faforever.api.error;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ApiException extends RuntimeException {

  private final Error[] errors;

  public ApiException(Error error) {
    this.errors = new Error[]{error};
  }

  public ApiException(Error[] errors) {
    this.errors = errors;
  }
}
