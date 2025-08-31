package com.faforever.api.data.util;

import com.yahoo.elide.core.security.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Stream;

import static com.faforever.api.data.domain.GroupPermission.ROLE_SCRAPER;
import static com.faforever.api.data.util.ElidePageSizeUtil.PAGE_LIMIT_PARAM;
import static com.faforever.api.data.util.ElidePageSizeUtil.PAGE_SIZE_PARAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ElidePageSizeUtilTest {

  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final int MAX_PAGE_SIZE = 10000;

  @ParameterizedTest
  @MethodSource("validationScenarios")
  void testValidateAndAdjustPageSize(MultiValueMap<String, String> params, User user,
    List<String> expectedSize, List<String> expectedLimit) {
    ElidePageSizeUtil.validateAndAdjustPageSize(params, user, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);

    assertEquals(expectedSize, params.get(PAGE_SIZE_PARAM));
    assertEquals(expectedLimit, params.get(PAGE_LIMIT_PARAM));
  }

  private static Stream<Arguments> validationScenarios() {
    return Stream.of(
      Arguments.of(new LinkedMultiValueMap<String, String>(), createNormalUser(), null, null),
      Arguments.of(new LinkedMultiValueMap<String, String>(), createScraperUser(), null, null),

      // Within limits
      createArgsWithExpected(PAGE_SIZE_PARAM, "50", createNormalUser(), List.of("50"), null),
      createArgsWithExpected(PAGE_SIZE_PARAM, "5000", createScraperUser(), List.of("5000"),
        null),

      // Invalid integer
      createArgsWithExpected(PAGE_SIZE_PARAM, "invalid", createNormalUser(),
        List.of("invalid"), null),

      // Edge cases
      createArgsWithExpected(PAGE_SIZE_PARAM, "-100", createNormalUser(), List.of("-100"),
        null),
      createArgsWithExpected(PAGE_SIZE_PARAM, "0", createNormalUser(), List.of("0"), null),
      createArgsWithExpected(PAGE_SIZE_PARAM, "", createNormalUser(), List.of(""), null),

      // With multiple values for param
      createMultipleArgsWithExpected(PAGE_SIZE_PARAM, "50", "150", createNormalUser(),
        List.of("50", "150"), null),
      createMultipleArgsWithExpected(PAGE_SIZE_PARAM, "invalid", "150", createNormalUser(),
        List.of("invalid", "150"), null),

      // With both params
      createArgsWithExpected(PAGE_LIMIT_PARAM, "50", createNormalUser(), null, List.of("50")),
      createBothArgsWithExpected("50", "150", createNormalUser(), List.of("50"),
        List.of("100")),

      // Over limits (previously failure scenarios)
      createArgsWithExpected(PAGE_SIZE_PARAM, "150", createNormalUser(), List.of("100"),
        null),
      createArgsWithExpected(PAGE_SIZE_PARAM, "15000", createScraperUser(), List.of("10000"),
        null),
      createArgsWithExpected(PAGE_LIMIT_PARAM, "150", createNormalUser(), null,
        List.of("100")),

      // Additional multiples for over limit adjustment
      createMultipleArgsWithExpected(PAGE_SIZE_PARAM, "150", "50", createNormalUser(),
        List.of("100"), null),
      createMultipleArgsWithExpected(PAGE_SIZE_PARAM, "200", "invalid", createNormalUser(),
        List.of("100"), null),
      createMultipleArgsWithExpected(PAGE_SIZE_PARAM, "invalid", "200", createNormalUser(),
        List.of("invalid", "200"), null)
    );
  }

  private static Arguments createArgsWithExpected(String paramName, String paramValue, User user,
    List<String> expSize, List<String> expLimit) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(paramName, paramValue);
    return Arguments.of(params, user, expSize, expLimit);
  }

  private static Arguments createMultipleArgsWithExpected(String paramName, String firstValue,
    String secondValue, User user, List<String> expSize, List<String> expLimit) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(paramName, firstValue);
    params.add(paramName, secondValue);
    return Arguments.of(params, user, expSize, expLimit);
  }

  private static Arguments createBothArgsWithExpected(String sizeValue, String limitValue,
    User user,
    List<String> expSize, List<String> expLimit) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(PAGE_SIZE_PARAM, sizeValue);
    params.add(PAGE_LIMIT_PARAM, limitValue);
    return Arguments.of(params, user, expSize, expLimit);
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
