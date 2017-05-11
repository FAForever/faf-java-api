package com.faforever.api.error;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ErrorResponse {
  private final ArrayList<ErrorResult> errors = new ArrayList<>();

  public ErrorResponse addError(ErrorResult newError) {
    errors.add(newError);
    return this;
  }
}
