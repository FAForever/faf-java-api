package com.faforever.api.error;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.Arrays;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.hasItemInArray;

public final class ApiExceptionMatcher {

  public static Matcher<ApiException> hasErrorCodes(ErrorCode... errorCodes) {
    return new FeatureMatcher<>(arrayContainingInAnyOrder(errorCodes), "error codes", "error codes") {
      @Override
      protected ErrorCode[] featureValueOf(ApiException actual) {
        return Arrays.stream(actual.getErrors())
          .map(Error::getErrorCode)
          .toArray(ErrorCode[]::new);
      }
    };
  }

  public static Matcher<ApiException> hasErrorCode(ErrorCode errorCode) {
    return new FeatureMatcher<>(hasItemInArray(errorCode), "error code", "error code") {
      @Override
      protected ErrorCode[] featureValueOf(ApiException actual) {
        return Arrays.stream(actual.getErrors())
          .map(Error::getErrorCode)
          .toArray(ErrorCode[]::new);
      }
    };
  }
}
