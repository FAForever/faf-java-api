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
  public static final String USERNAME = "junit";
  public static final String EMAIL = "junit@example.com";
  public static final String FROM_EMAIL = "foo@bar.com";
  public static final String FROM_NAME = "foobar";
  public static final String SUBJECT = "Hello";
  public static final String HTML_BODY = "someHtmlBody";
  private EmailService instance;
  private FafApiProperties properties;

  @Mock
  private DomainBlacklistRepository domainBlacklistRepository;
  @Mock
  private EmailSender emailSender;
  @Mock
  private MailBodyBuilder mailBodyBuilder;

  @BeforeEach
  public void setUp() {
    properties = new FafApiProperties();
    properties.getMail().setFromEmailAddress(FROM_EMAIL);
    properties.getMail().setFromEmailName(FROM_NAME);

    instance = new EmailService(domainBlacklistRepository, properties, emailSender, mailBodyBuilder);
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
  public void sendActivationMail() throws Exception{
    Registration registration = properties.getRegistration();
    registration.setSubject(SUBJECT);

    when(mailBodyBuilder.buildAccountActivationBody(USERNAME, ACTION_URL)).thenReturn(HTML_BODY);

    instance.sendActivationMail(USERNAME, EMAIL, ACTION_URL);

    verify(emailSender).sendMail(FROM_EMAIL, FROM_NAME, EMAIL, SUBJECT, HTML_BODY);
  }

  @Test
  public void sendPasswordResetMail() throws Exception {
    PasswordReset passwordReset = properties.getPasswordReset();
    passwordReset.setSubject(SUBJECT);

    when(mailBodyBuilder.buildPasswordResetBody(USERNAME, ACTION_URL)).thenReturn(HTML_BODY);

    instance.sendPasswordResetMail(USERNAME, EMAIL, ACTION_URL);

    verify(emailSender).sendMail(FROM_EMAIL, FROM_NAME, EMAIL, SUBJECT, HTML_BODY);
  }
}
