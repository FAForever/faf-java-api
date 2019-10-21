package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.PasswordReset;
import com.faforever.api.config.FafApiProperties.Registration;
import com.faforever.api.error.ApiExceptionMatcher;
import com.faforever.api.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.ExpectedExceptionSupport;
import org.junit.rules.ExpectedException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ExpectedExceptionSupport.class})
public class EmailServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private EmailService instance;
  private FafApiProperties properties;

  @Mock
  private DomainBlacklistRepository domainBlacklistRepository;
  @Mock
  private EmailSender emailSender;

  @BeforeEach
  public void setUp() throws Exception {
    properties = new FafApiProperties();
    properties.getMail().setFromEmailAddress("foo@bar.com");
    properties.getMail().setFromEmailName("foobar");

    instance = new EmailService(domainBlacklistRepository, properties, emailSender);
  }

  @Test
  public void validateEmailAddress() throws Exception {
    instance.validateEmailAddress("test@example.com");
  }

  @Test
  public void validateEmailAddressMissingAt() throws Exception {
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.EMAIL_INVALID));
    instance.validateEmailAddress("testexample.com");
  }

  @Test
  public void validateEmailAddressMissingTld() throws Exception {
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.EMAIL_INVALID));
    instance.validateEmailAddress("test@example");
  }

  @Test
  public void validateEmailAddressBlacklisted() throws Exception {
    when(domainBlacklistRepository.existsByDomain("example.com")).thenReturn(true);
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.EMAIL_BLACKLISTED));

    instance.validateEmailAddress("test@example.com");
  }

  @Test
  public void sendActivationMail() throws Exception {
    Registration registration = properties.getRegistration();
    registration.setSubject("Hello");
    registration.setHtmlFormat("Hello {0}, bla: {1}");

    instance.sendActivationMail("junit", "junit@example.com", "http://example.com");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "junit@example.com", "Hello", "Hello junit, bla: http://example.com");
  }

  @Test
  public void sendPasswordResetMail() {
    PasswordReset passwordReset = properties.getPasswordReset();
    passwordReset.setSubject("Hello");
    passwordReset.setHtmlFormat("Hello {0}, bla: {1}");

    instance.sendPasswordResetMail("junit", "junit@example.com", "http://example.com");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "junit@example.com", "Hello", "Hello junit, bla: http://example.com");
  }
}
