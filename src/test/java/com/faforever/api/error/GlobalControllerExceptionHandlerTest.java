package com.faforever.api.error;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

public class GlobalControllerExceptionHandlerTest {
  private static final String COMMON_MESSAGE = "Error";
  private GlobalControllerExceptionHandler instance;

  @BeforeEach
  public void setUp() {
    instance = new GlobalControllerExceptionHandler();
  }

  @Test
  public void testErrorFormatting() {
    ApiException ex = new ApiException(new Error(ErrorCode.CLAN_NAME_EXISTS, "My Clan"));

    ErrorResponse response = instance.processApiException(ex);

    assertEquals(1, response.getErrors().size());
    final ErrorResult errorResult = response.getErrors().get(0);
    assertEquals(ErrorCode.CLAN_NAME_EXISTS.getTitle(), errorResult.getTitle());
    assertEquals(MessageFormat.format(ErrorCode.CLAN_NAME_EXISTS.getDetail(), "My Clan"), errorResult.getDetail());
    assertEquals(String.valueOf(ErrorCode.CLAN_NAME_EXISTS.getCode()), errorResult.getAppCode());
  }

  @Test
  public void testErrorFormattingNull() {
    ApiException ex = new ApiException(new Error(ErrorCode.CLAN_NAME_EXISTS));

    ErrorResponse response = instance.processApiException(ex);

    assertEquals(1, response.getErrors().size());
    final ErrorResult errorResult = response.getErrors().get(0);
    assertEquals(ErrorCode.CLAN_NAME_EXISTS.getTitle(), errorResult.getTitle());
    assertEquals(MessageFormat.format(ErrorCode.CLAN_NAME_EXISTS.getDetail(), new Object[0]), errorResult.getDetail());
    assertEquals(String.valueOf(ErrorCode.CLAN_NAME_EXISTS.getCode()), errorResult.getAppCode());
  }

  @Test
  public void testProgrammingError() {
    ProgrammingError ex = new ProgrammingError(COMMON_MESSAGE);

    ErrorResponse response = instance.processProgrammingError(ex);

    assertEquals(1, response.getErrors().size());
    final ErrorResult errorResult = response.getErrors().get(0);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), errorResult.getTitle());
    assertEquals(COMMON_MESSAGE, errorResult.getDetail());
  }

  @ParameterizedTest
  @MethodSource(value = "servletExceptionSource")
  public void testProcessResourceNotFoundException(final ServletException ex) {
    ErrorResponse response = instance.processResourceNotFoundException(ex);

    assertEquals(1, response.getErrors().size());
    final ErrorResult errorResult = response.getErrors().get(0);

    assertAll(
      () -> assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), errorResult.getTitle()),
      () -> assertEquals(ex.getMessage(), errorResult.getDetail())
    );
  }

  @Test
  public void testValidationException() {
    final ValidationException ex = new ValidationException(COMMON_MESSAGE);

    ErrorResponse response = instance.processValidationException(ex);

    assertEquals(1, response.getErrors().size());
    final ErrorResult errorResult = response.getErrors().get(0);
    assertEquals(ErrorCode.VALIDATION_FAILED.getTitle(), errorResult.getTitle());
    assertEquals(COMMON_MESSAGE, errorResult.getDetail());
    assertEquals(String.valueOf(ErrorCode.VALIDATION_FAILED.getCode()), errorResult.getAppCode());
  }

  @Test
  public void testConstraintViolationException() {
    ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
    final Validator validator = vf.getValidator();

    final Player player = new Player();
    player.setId(1);
    player.setLogin("Player");

    final Clan clan = new Clan();
    clan.setName("Clan");
    clan.setLeader(player);
    clan.setFounder(player);
    clan.setMemberships(Set.of());

    final Set<ConstraintViolation<Clan>> constraintViolations = validator.validate(clan);
    final ConstraintViolationException ex = new ConstraintViolationException(COMMON_MESSAGE, constraintViolations);

    ErrorResponse response = instance.processConstraintViolationException(ex);

    assertEquals(3, response.getErrors().size());
    final ErrorResult errorResult = response.getErrors().get(0);
    assertEquals(ErrorCode.VALIDATION_FAILED.getTitle(), errorResult.getTitle());
    assertEquals(String.valueOf(ErrorCode.VALIDATION_FAILED.getCode()), errorResult.getAppCode());
  }

  public static Stream<Arguments> servletExceptionSource() {
    return Stream.of(
      Arguments.of(new HttpRequestMethodNotSupportedException(HttpMethod.DELETE.name())),
      Arguments.of(new NoResourceFoundException(HttpMethod.POST, "test/path"))
    );
  }
}
