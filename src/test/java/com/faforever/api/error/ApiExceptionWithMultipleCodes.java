package com.faforever.api.error;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Arrays;

public final class ApiExceptionWithMultipleCodes extends BaseMatcher<ApiException> {

  private final ErrorCode[] errorCode;

  private ApiExceptionWithMultipleCodes(ErrorCode... errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an ApiException with multiple errors: " + Arrays.toString(errorCode));
  }

  @Override
  public boolean matches(Object item) {
    ApiException apiException = (ApiException) item;
    return Arrays.deepEquals(errorCode,
        Arrays.asList(apiException.getErrors()).stream().map(Error::getErrorCode).toArray());

  }

  public static ApiExceptionWithMultipleCodes apiExceptionWithCode(ErrorCode... errorCode) {
    return new ApiExceptionWithMultipleCodes(errorCode);
  }
}
