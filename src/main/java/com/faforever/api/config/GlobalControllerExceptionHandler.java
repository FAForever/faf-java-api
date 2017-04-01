package com.faforever.api.config;

import com.faforever.api.config.error.ErrorResponse;
import com.faforever.api.config.error.ErrorResult;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ValidationException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

@ControllerAdvice
class GlobalControllerExceptionHandler {
  private Map<String, Serializable> errorResponse(String title, String message) {
    ImmutableMap<String, Serializable> error = ImmutableMap.of(
        "title", title,
        "detail", message);
    return ImmutableMap.of("errors", new ImmutableMap[]{error});
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public Map<String, Serializable> processValidationException(ValidationException ex) {
    return errorResponse(ErrorCode.VALIDATION_FAILED.getTitle(), ex.getMessage());
  }

  @ExceptionHandler(ApiException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResponse processApiException(ApiException ex) {
    ErrorResponse response = new ErrorResponse();
    Arrays.stream(ex.getErrors()).forEach(error -> {
      ErrorCode code = error.getErrorCode();
      response.addError(new ErrorResult(code.getTitle(), MessageFormat.format(code.getDetail(), error.getArgs())));
    });
    return response;
  }
}
