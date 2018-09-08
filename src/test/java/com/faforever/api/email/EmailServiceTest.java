package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Ban;
import com.faforever.api.config.FafApiProperties.PasswordReset;
import com.faforever.api.config.FafApiProperties.Registration;
import com.faforever.api.config.FafApiProperties.UsernameChange;
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
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_INVALID));
    instance.validateEmailAddress("testexample.com");
  }

  @Test
  public void validateEmailAddressMissingTld() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_INVALID));
    instance.validateEmailAddress("test@example");
  }

  @Test
  public void validateEmailAddressBlacklisted() {
    when(domainBlacklistRepository.existsByDomain("example.com")).thenReturn(true);
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_BLACKLISTED));

    instance.validateEmailAddress("test@example.com");
  }

  @Test
  public void sendActivationMail() {
    Registration registration = properties.getRegistration();
    registration.setSubject("Hello");
    registration.setHtmlFormat("Hello {0}, bla: {1}");

    instance.sendActivationMail("junit", "junit@example.com", "http://example.com");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "junit@example.com", "Hello", "Hello junit, bla: http://example.com");
  }

  @Test
  public void sendUsernameChangeMail() {
    UsernameChange usernameChange = properties.getUsernameChange();
    usernameChange.setMailSubject("Hello");
    usernameChange.setMailBody("Hello {0}, you changed your name to: {1}");

    instance.sendUsernameChangeMail("alex@gmail.com", "axel12", "phil12");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "alex@gmail.com", "Hello", "Hello axel12, you changed your name to: phil12");
  }

  @Test
  public void sendBanMail() {
    Ban ban = properties.getBan();
    ban.setBanMailSubject("Hello");
    ban.setBanMailBody("Hello {0},\nYour account was banned\nReason - {1}\nBanner - {2}\nTime - {3}\nType - {4}\nExpires - {5}.\nThank you for your fairness and acceptance.");

    instance.sendBanMail("alex@gmail.com", "axel12", "reason","banner","createTime","type","expires");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "alex@gmail.com", "Hello", "Hello axel12,\nYour account was banned\nReason - reason\nBanner - banner\nTime - createTime\nType - type\nExpires - expires.\nThank you for your fairness and acceptance.");
  }

  @Test
  public void sendBanRevokeMail() {
    Ban ban = properties.getBan();
    ban.setBanRevokeMailSubject("Hello");
    ban.setBanRevokeMailBody("Hello {0},\nYour account was been unbanned.\nModerator that unbanned you - {1}\nReason - {2}\nOriginal reason of the ban - {3}\noriginal banner - {4}\nTime of original ban - {5}.\nThank you for your fairness and acceptance.");

    instance.sendBanRevokeMail("alex@gmail.com", "axel12", "mod1","reason1","reason2","mod2","time");

    verify(emailSender).sendMail("foo@bar.com", "foobar", "alex@gmail.com", "Hello", "Hello axel12,\nYour account was been unbanned.\nModerator that unbanned you - mod1\nReason - reason1\nOriginal reason of the ban - reason2\noriginal banner - mod2\nTime of original ban - time.\nThank you for your fairness and acceptance.");
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
