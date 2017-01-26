package com.faforever.api.error;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Error {

  private final ErrorCode errorCode;
  private final Object[] args;

  public Error(ErrorCode errorCode, Object... args) {
    this.errorCode = errorCode;
    this.args = args;
  }
}
