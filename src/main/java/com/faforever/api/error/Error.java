package com.faforever.api.error;

import lombok.Value;

@Value
public class Error {

  ErrorCode errorCode;
  Object[] args;

  public Error(ErrorCode errorCode, Object... args) {
    this.errorCode = errorCode;
    this.args = args;
  }
}
