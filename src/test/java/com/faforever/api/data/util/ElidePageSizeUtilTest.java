package com.faforever.api.data.util;

import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.yahoo.elide.core.security.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.faforever.api.data.domain.GroupPermission.ROLE_SCRAPER;
import static com.faforever.api.data.util.ElidePageSizeUtil.PAGE_LIMIT_PARAM;
import static com.faforever.api.data.util.ElidePageSizeUtil.PAGE_SIZE_PARAM;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ElidePageSizeUtilTest {

  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final int MAX_PAGE_SIZE = 10000;

  @ParameterizedTest
  @MethodSource("successfulValidationScenarios")
  void validatePageSizeSuccessful(MultiValueMap<String, String> params, User user) {
    assertDoesNotThrow(
      () -> ElidePageSizeUtil.validatePageSize(params, user, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE));
  }

  private static Stream<Arguments> successfulValidationScenarios() {
    return Stream.of(
      // Empty args
      Arguments.of(new LinkedMultiValueMap<String, String>(), createNormalUser()),
      Arguments.of(new LinkedMultiValueMap<String, String>(), createScraperUser()),

      // Within limits
      createArguments(PAGE_SIZE_PARAM, "50", createNormalUser()),
      createArguments(PAGE_SIZE_PARAM, "5000", createScraperUser()),

      // Invalid integer
      createArguments(PAGE_SIZE_PARAM, "invalid", createNormalUser()),

      // Edge cases
      createArguments(PAGE_SIZE_PARAM, "-100", createNormalUser()),
      createArguments(PAGE_SIZE_PARAM, "0", createNormalUser()),
      createArguments(PAGE_SIZE_PARAM, "", createNormalUser()),

      // With multiple values for value
      createMultipleArguments(PAGE_SIZE_PARAM, "50", "150", createNormalUser()),
      createMultipleArguments(PAGE_SIZE_PARAM, "invalid", "150", createNormalUser()),

      // With both arguments
      createArguments(PAGE_LIMIT_PARAM, "50", createNormalUser()),
      createBothArguments("50", "150", createNormalUser())
    );
  }

  @ParameterizedTest
  @MethodSource("failureValidationScenarios")
  void validatePageSizeFailure(MultiValueMap<String, String> params, User user) {
    final ApiException exception = assertThrows(ApiException.class,
      () -> ElidePageSizeUtil.validatePageSize(params, user, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE));

    assertTrue(Arrays.stream(exception.getErrors())
      .anyMatch(error -> ErrorCode.QUERY_INVALID_PAGE_SIZE == error.getErrorCode()));
  }

  private static Stream<Arguments> failureValidationScenarios() {
    return Stream.of(
      // Over limits
      createArguments(PAGE_SIZE_PARAM, "150", createNormalUser()),
      createArguments(PAGE_SIZE_PARAM, "15000", createScraperUser()),
      createArguments(PAGE_LIMIT_PARAM, "150", createNormalUser())
    );
  }

  private static Arguments createArguments(String paramName, String paramValue, User user) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(paramName, paramValue);
    return Arguments.of(params, user);
  }

  private static Arguments createMultipleArguments(String paramName, String firstValue,
    String secondValue, User user) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(paramName, firstValue);
    params.add(paramName, secondValue);
    return Arguments.of(params, user);
  }

  private static Arguments createBothArguments(String sizeValue, String limitValue, User user) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(PAGE_SIZE_PARAM, sizeValue);
    params.add(PAGE_LIMIT_PARAM, limitValue);
    return Arguments.of(params, user);
  }

  private static User createNormalUser() {
    final User normalUserMock = Mockito.mock(User.class);
    lenient().when(normalUserMock.isInRole(ROLE_SCRAPER)).thenReturn(false);

    return normalUserMock;
  }

  private static User createScraperUser() {
    final User scraperUserMock = Mockito.mock(User.class);
    lenient().when(scraperUserMock.isInRole(ROLE_SCRAPER)).thenReturn(true);

    return scraperUserMock;
  }
}
