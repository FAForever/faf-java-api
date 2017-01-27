package com.faforever.api.error;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Arrays;

public final class ApiExceptionWithMutlipleCodes extends BaseMatcher<ApiException> {

  private final ErrorCode[] errorCode;

  private ApiExceptionWithMutlipleCodes(ErrorCode... errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an ApiException with exactly multiple errors: " + Arrays.toString(errorCode));
  }

  @Override
  public boolean matches(Object item) {
    ApiException apiException = (ApiException) item;
    return Arrays.deepEquals(errorCode,
        Arrays.asList(apiException.getErrors()).stream().map(Error::getErrorCode).toArray());

  }

  public static ApiExceptionWithMutlipleCodes apiExceptionWithCode(ErrorCode... errorCode) {
    return new ApiExceptionWithMutlipleCodes(errorCode);
  }
}
