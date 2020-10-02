package com.faforever.api.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.CompletionException;

@ControllerAdvice
@Slf4j
class GlobalControllerExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResponse processConstraintViolationException(ConstraintViolationException ex) {
    log.debug("Constraint violation", ex);
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
    log.debug("Entity could not be processed", ex);
    return new ErrorResponse().addError(new ErrorResult(
      String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()),
      ErrorCode.VALIDATION_FAILED.getTitle(),
      ex.getMessage(),
      String.valueOf(ErrorCode.VALIDATION_FAILED.getCode()),
      null
    ));
  }

  @ExceptionHandler(NotFoundApiException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public ErrorResponse processNotFoundException(NotFoundApiException ex) {
    log.debug("Entity could not be found", ex);
    return createResponseFromApiException(ex, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ApiException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResponse processApiException(ApiException ex) {
    log.debug("API exception", ex);
    return createResponseFromApiException(ex, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(ProgrammingError.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse processProgrammingError(ProgrammingError ex) {
    log.warn("Programming error", ex);
    ErrorResponse response = new ErrorResponse();
    response.addError(new ErrorResult(
      String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
      HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
      ex.getMessage()
    ));
    return response;
  }


  @ExceptionHandler({AccessDeniedException.class, Forbidden.class})
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  public ErrorResponse processAccessDeniedException(Throwable ex) throws MissingServletRequestPartException {
    log.debug("Access denied", ex);

    ErrorResponse response = new ErrorResponse();
    response.addError(new ErrorResult(
      String.valueOf(HttpStatus.FORBIDDEN.value()),
      "You are not allowed to access this resource.",
      ex.getMessage()
    ));
    return response;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse processException(Throwable ex) throws MissingServletRequestPartException {
    // If we don't rethrow, oauth authX is broken
    if (ex instanceof InsufficientAuthenticationException) {
      throw (InsufficientAuthenticationException) ex;
    }
    if (ex instanceof CompletionException) {
      throw (CompletionException) ex;
    }

    log.warn("Internal server error", ex);

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
    log.debug("Bad request", ex);
    ErrorResponse response = new ErrorResponse();
    response.addError(new ErrorResult(
      String.valueOf(HttpStatus.BAD_REQUEST.value()),
      ex.getClass().getName(),
      ex.getMessage()
    ));
    return response;
  }

  private ErrorResponse createResponseFromApiException(ApiException apiException, HttpStatus status) {
    ErrorResponse response = new ErrorResponse();
    Arrays.stream(apiException.getErrors()).forEach(error -> {
      ErrorCode errorCode = error.getErrorCode();
      final Object[] args = error.getArgs();
      response.addError(new ErrorResult(
        String.valueOf(status.value()),
        errorCode.getTitle(),
        MessageFormat.format(errorCode.getDetail(), args),
        String.valueOf(errorCode.getCode()),
        ErrorResult.createMeta(args, null).orElse(null)
      ));
    });
    return response;
  }
}
