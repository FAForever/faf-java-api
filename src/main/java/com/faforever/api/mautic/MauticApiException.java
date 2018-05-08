package com.faforever.api.mautic;

import com.faforever.api.mautic.MauticErrorResponse.Error;
import lombok.Getter;

import java.util.List;

@Getter
public class MauticApiException extends RuntimeException {
  private final List<Error> errors;

  public MauticApiException(List<Error> errors) {
    super(errors.toString());
    this.errors = errors;
  }
}
