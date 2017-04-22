package com.faforever.api.error;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.text.MessageFormat;
import java.util.Arrays;

@ControllerAdvice
class GlobalControllerExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResponse processConstraintViolationException(ConstraintViolationException ex) {
    final ErrorResponse errorResponse = new ErrorResponse();
    ex.getConstraintViolations().forEach(constraintViolation -> {
      String detail = constraintViolation.getMessage();
      if (!constraintViolation.getPropertyPath().toString().isEmpty()) {
        detail += String.format(" [property: %s]", constraintViolation.getPropertyPath());
      }
      errorResponse.addError(new ErrorResult(
        String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()),
        ErrorCode.VALIDATION_FAILED.getTitle(),
        detail,
        String.valueOf(ErrorCode.VALIDATION_FAILED.getCode()),
        ErrorResult.createMeta(constraintViolation.getExecutableParameters(), null).orElse(null)
      ));
    });

    return errorResponse;
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResponse processValidationException(ValidationException ex) {
    return new ErrorResponse().addError(new ErrorResult(
      String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()),
      ErrorCode.VALIDATION_FAILED.getTitle(),
      ex.getMessage(),
      String.valueOf(ErrorCode.VALIDATION_FAILED.getCode()),
      null
    ));
  }

  @ExceptionHandler(ApiException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResponse processApiException(ApiException ex) {
    ErrorResponse response = new ErrorResponse();
    Arrays.stream(ex.getErrors()).forEach(error -> {
      ErrorCode errorCode = error.getErrorCode();
      final Object[] args = error.getArgs();
      response.addError(new ErrorResult(
        String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()),
        errorCode.getTitle(),
        MessageFormat.format(errorCode.getDetail(), args),
        String.valueOf(errorCode.getCode()),
        ErrorResult.createMeta(args, null).orElse(null)
      ));
    });
    return response;
  }

  @ExceptionHandler(ProgrammingError.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse processProgrammingError(ProgrammingError ex) {
    ErrorResponse response = new ErrorResponse();
    response.addError(new ErrorResult(
      String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
      HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
      ex.getMessage()
    ));
    return response;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse processException(Exception ex) throws MissingServletRequestPartException {
    //if we don't rethrow, oauth authX is broken
    if (ex instanceof InsufficientAuthenticationException) {
      throw (InsufficientAuthenticationException) ex;
    }

    ErrorResponse response = new ErrorResponse();
    response.addError(new ErrorResult(
      String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
      ex.getClass().getName(),
      ex.getMessage()
    ));
    return response;
  }

  @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorResponse processBadRequests(Exception ex) throws Exception {
    ErrorResponse response = new ErrorResponse();
    response.addError(new ErrorResult(
      String.valueOf(HttpStatus.BAD_REQUEST.value()),
      ex.getClass().getName(),
      ex.getMessage()
    ));
    return response;
  }
}
