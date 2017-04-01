package com.faforever.api.config;

import com.faforever.api.config.error.ErrorResponse;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorTest {
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
    assertEquals("Clan Name already in use", response.getErrors().get(0).getTitle());
    assertEquals("The clan name 'My Clan' is already in use. Please choose a different clan name.", response.getErrors().get(0).getDetail());
  }

  @Test
  public void testErrorFormattingNull() {
    ApiException ex = new ApiException(new Error(ErrorCode.CLAN_NAME_EXISTS));
    ErrorResponse response = instance.processApiException(ex);
    assertEquals(1, response.getErrors().size());
    assertEquals("Clan Name already in use", response.getErrors().get(0).getTitle());
    assertEquals("The clan name '{0}' is already in use. Please choose a different clan name.", response.getErrors().get(0).getDetail());
  }
}
