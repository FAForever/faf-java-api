package com.faforever.api.error;

import com.faforever.api.logging.RequestIdFilter;
import lombok.Data;
import org.slf4j.MDC;

import java.util.ArrayList;

@Data
public class ErrorResponse {
  private final String requestId;

  private final ArrayList<ErrorResult> errors = new ArrayList<>();

  public ErrorResponse() {
    requestId = MDC.get(RequestIdFilter.REQUEST_ID_KEY);
  }

  public ErrorResponse addError(ErrorResult newError) {
    errors.add(newError);
    return this;
  }
}
