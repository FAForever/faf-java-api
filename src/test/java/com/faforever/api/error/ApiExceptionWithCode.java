package com.faforever.api.error;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public final class ApiExceptionWithCode extends BaseMatcher<ApiException> {

  private final ErrorCode errorCode;

  private ApiExceptionWithCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an ApiException with exactly one error: " + errorCode);
  }

  @Override
  public boolean matches(Object item) {
    ApiException apiException = (ApiException) item;
    return apiException.getErrors().length == 1
        && apiException.getErrors()[0].getErrorCode() == errorCode;
  }

  public static ApiExceptionWithCode apiExceptionWithCode(ErrorCode errorCode) {
    return new ApiExceptionWithCode(errorCode);
  }
}
