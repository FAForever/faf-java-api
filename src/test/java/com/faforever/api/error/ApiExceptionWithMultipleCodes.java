package com.faforever.api.error;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

public final class ApiExceptionWithMultipleCodes extends TypeSafeMatcher<ApiException> {

  public static Matcher<ApiException> hasErrorCodes(ErrorCode... errorCodes) {
    return new FeatureMatcher<ApiException, ErrorCode[]>(arrayContainingInAnyOrder(errorCodes), "error codes", "error codes") {
      @Override
      protected ErrorCode[] featureValueOf(ApiException actual) {
        return Arrays.stream(actual.getErrors())
          .map(Error::getErrorCode)
          .toArray(ErrorCode[]::new);
      }
    };
  }

  private final ErrorCode[] errorCode;

  private ApiExceptionWithMultipleCodes(ErrorCode... errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an ApiException with multiple errors: " + Arrays.toString(errorCode));
  }

  @Override
  protected boolean matchesSafely(ApiException item) {
    return Arrays.deepEquals(errorCode,
      Arrays.stream(item.getErrors()).map(Error::getErrorCode).toArray());
  }

  public static ApiExceptionWithMultipleCodes apiExceptionWithCode(ErrorCode... errorCode) {
    return new ApiExceptionWithMultipleCodes(errorCode);
  }
}
