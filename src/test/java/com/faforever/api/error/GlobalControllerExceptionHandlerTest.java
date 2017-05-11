package com.faforever.api.error;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import javax.validation.*;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GlobalControllerExceptionHandlerTest {
  private static final String COMMON_MESSAGE = "Error";
  private GlobalControllerExceptionHandler instance;

  @Before
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
    assertEquals(MessageFormat.format(ErrorCode.CLAN_NAME_EXISTS.getDetail(), null), errorResult.getDetail());
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
    clan.setMemberships(Collections.emptyList());

    final Set<ConstraintViolation<Clan>> constraintViolations = validator.validate(clan);
    final ConstraintViolationException ex = new ConstraintViolationException(COMMON_MESSAGE, constraintViolations);

    ErrorResponse response = instance.processConstraintViolationException(ex);

    assertEquals(3, response.getErrors().size());
    final ErrorResult errorResult = response.getErrors().get(0);
    assertEquals(ErrorCode.VALIDATION_FAILED.getTitle(), errorResult.getTitle());
    assertEquals(String.valueOf(ErrorCode.VALIDATION_FAILED.getCode()), errorResult.getAppCode());
  }
}
