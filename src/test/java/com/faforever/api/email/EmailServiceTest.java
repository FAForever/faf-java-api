package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.PasswordReset;
import com.faforever.api.config.FafApiProperties.Registration;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

  public static final String ACTION_URL = "https://example.com";
  private EmailService instance;
  private FafApiProperties properties;

  @Mock
  private DomainBlacklistRepository domainBlacklistRepository;
  @Mock
  private EmailSender emailSender;

  @BeforeEach
  public void setUp() {
    properties = new FafApiProperties();
    properties.getMail().setFromEmailAddress("foo@bar.com");
    properties.getMail().setFromEmailName("foobar");

    instance = new EmailService(domainBlacklistRepository, properties, emailSender);
  }

  @Test
  public void validateEmailAddress() {
    instance.validateEmailAddress("test@example.com");
  }

  @Test
  public void validateEmailAddressMissingAt() {
    ApiException result = assertThrows(ApiException.class, () -> instance.validateEmailAddress("testexample.com"));
    assertThat(result, hasErrorCode(ErrorCode.EMAIL_INVALID));
  }

  @Test
  public void validateEmailAddressMissingTld() {
    ApiException result = assertThrows(ApiException.class, () -> instance.validateEmailAddress("test@example"));
    assertThat(result, hasErrorCode(ErrorCode.EMAIL_INVALID));
  }

  @Test
  public void validateEmailAddressBlacklisted() {
    when(domainBlacklistRepository.existsByDomain("example.com")).thenReturn(true);
    ApiException result = assertThrows(ApiException.class, () -> instance.validateEmailAddress("test@example.com"));
    assertThat(result, hasErrorCode(ErrorCode.EMAIL_BLACKLISTED));
  }

  @Test
  public void sendActivationMail() {
    Registration registration = properties.getRegistration();
    registration.setSubject("Hello");
    registration.setHtmlFormat("Hello {0}, bla: {1}");

    instance.sendActivationMail("junit", "junit@example.com", ACTION_URL);

    verify(emailSender).sendMail("foo@bar.com", "foobar", "junit@example.com", "Hello", "Hello junit, bla: " + ACTION_URL);
  }

  @Test
  public void sendPasswordResetMail() {
    PasswordReset passwordReset = properties.getPasswordReset();
    passwordReset.setSubject("Hello");
    passwordReset.setHtmlFormat("Hello {0}, bla: {1}");

    instance.sendPasswordResetMail("junit", "junit@example.com", ACTION_URL);

    verify(emailSender).sendMail("foo@bar.com", "foobar", "junit@example.com", "Hello", "Hello junit, bla: " + ACTION_URL);
  }
}
