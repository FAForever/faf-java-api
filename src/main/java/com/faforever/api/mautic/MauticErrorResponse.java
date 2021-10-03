package com.faforever.api.mautic;

import java.util.List;
import java.util.Map;

record MauticErrorResponse(List<Error> errors) {

  static record Error(
    int code,
    String message,
    Map<String, Object> details
  ) {
  }
}
