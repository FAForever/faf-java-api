package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.PasswordReset;
import com.faforever.api.config.FafApiProperties.Registration;
import com.faforever.api.error.ApiExceptionWithCode;
import com.faforever.api.error.ErrorCode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private EmailService instance;
  private FafApiProperties properties;

  @Mock
  private DomainBlacklistRepository domainBlacklistRepository;
  @Mock
  private EmailSender emailSender;

  @Before
  public void setUp() throws Exception {
    properties = new FafApiProperties();

    instance = new EmailService(domainBlacklistRepository, properties, emailSender);
  }

  @Test
  public void validateEmailAddress() throws Exception {
    instance.validateEmailAddress("test@example.com");
  }

  @Test
  public void validateEmailAddressMissingAt() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_INVALID));
    instance.validateEmailAddress("testexample.com");
  }

  @Test
  public void validateEmailAddressMissingTld() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_INVALID));
    instance.validateEmailAddress("test@example");
  }

  @Test
  public void validateEmailAddressBlacklisted() throws Exception {
    when(domainBlacklistRepository.existsByDomain("example.com")).thenReturn(true);
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_BLACKLISTED));

    instance.validateEmailAddress("test@example.com");
  }

  @Test
  public void sendActivationMail() throws Exception {
    Registration registration = properties.getRegistration();
    registration.setFromEmail("foo@bar.com");
    registration.setFromName("foobar");
    registration.setSubject("Hello");
    registration.setHtmlFormat("Hello %s, bla: %s");

    instance.sendActivationMail("junit", "junit@example.com", "http://example.com");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "junit@example.com", "Hello", "Hello junit, bla: http://example.com");
  }

  @Test
  public void sendPasswordResetMail() {
    PasswordReset passwordReset = properties.getPasswordReset();
    passwordReset.setFromEmail("foo@bar.com");
    passwordReset.setFromName("foobar");
    passwordReset.setSubject("Hello");
    passwordReset.setHtmlFormat("Hello %s, bla: %s");

    instance.sendPasswordResetMail("junit", "junit@example.com", "http://example.com");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "junit@example.com", "Hello", "Hello junit, bla: http://example.com");
  }
}
