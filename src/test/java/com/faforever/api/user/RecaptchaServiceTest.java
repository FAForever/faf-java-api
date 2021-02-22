package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.user.RecaptchaService.VerifyResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static com.faforever.api.user.RecaptchaService.RECAPTCHA_VERIFICATION_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecaptchaServiceTest {
  private final static String RECAPTCHA_RESPONSE = "recaptchaResponse";
  private final static String RECAPTCHA_SECRET = "recaptchaSecret";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  FafApiProperties fafApiProperties;

  @Mock
  RestTemplateBuilder restTemplateBuilder;

  @Mock
  RestTemplate restTemplate;

  RecaptchaService instance;

  @BeforeEach
  void beforeEach() {
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    instance = new RecaptchaService(fafApiProperties, restTemplateBuilder);
  }

  @AfterEach
  void afterEach() {
    verifyNoMoreInteractions(restTemplateBuilder, restTemplate);
  }

  @Test
  void skipOnDisabled() {
    when(fafApiProperties.getRecaptcha().isEnabled()).thenReturn(false);

    instance.validateResponse(null);
    instance.validateResponse(RECAPTCHA_RESPONSE);
  }

  @Test
  void failOnEnabledAndNullInputRepsonse() {
    when(fafApiProperties.getRecaptcha().isEnabled()).thenReturn(true);

    ApiException error = assertThrows(ApiException.class, () -> instance.validateResponse(null));

    assertThat(error, hasErrorCode(ErrorCode.RECAPTCHA_VALIDATION_FAILED));
  }

  @Test
  void failOnEnabledAndNullOutputRepsonse() {
    when(fafApiProperties.getRecaptcha().isEnabled()).thenReturn(true);
    when(fafApiProperties.getRecaptcha().getSecret()).thenReturn(RECAPTCHA_SECRET);
    when(restTemplate.postForObject(
      RECAPTCHA_VERIFICATION_URL,
      null,
      RecaptchaService.VerifyResponse.class,
      Map.of("secret", RECAPTCHA_SECRET, "response", RECAPTCHA_RESPONSE)
    )).thenReturn(null);

    ApiException error = assertThrows(ApiException.class, () -> instance.validateResponse(RECAPTCHA_RESPONSE));

    assertThat(error, hasErrorCode(ErrorCode.RECAPTCHA_VALIDATION_FAILED));
  }

  @Test
  void failOnEnabledAndNoSuccessInResponse() {
    when(fafApiProperties.getRecaptcha().isEnabled()).thenReturn(true);
    when(fafApiProperties.getRecaptcha().getSecret()).thenReturn(RECAPTCHA_SECRET);
    when(restTemplate.postForObject(
      RECAPTCHA_VERIFICATION_URL,
      null,
      RecaptchaService.VerifyResponse.class,
      Map.of("secret", RECAPTCHA_SECRET, "response", RECAPTCHA_RESPONSE)
    )).thenReturn(new VerifyResponse(false, OffsetDateTime.MAX, "localhost", List.of()));

    ApiException error = assertThrows(ApiException.class, () -> instance.validateResponse(RECAPTCHA_RESPONSE));

    assertThat(error, hasErrorCode(ErrorCode.RECAPTCHA_VALIDATION_FAILED));
  }

  @Test
  void passOnEnabledAndSuccessInResponse() {
    when(fafApiProperties.getRecaptcha().isEnabled()).thenReturn(true);
    when(fafApiProperties.getRecaptcha().getSecret()).thenReturn(RECAPTCHA_SECRET);
    when(restTemplate.postForObject(
      RECAPTCHA_VERIFICATION_URL,
      null,
      RecaptchaService.VerifyResponse.class,
      Map.of("secret", RECAPTCHA_SECRET, "response", RECAPTCHA_RESPONSE)
    )).thenReturn(new VerifyResponse(true, OffsetDateTime.MAX, "localhost", List.of()));

    instance.validateResponse(RECAPTCHA_RESPONSE);
  }
}
