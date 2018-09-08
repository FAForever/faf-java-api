package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Ban;
import com.faforever.api.config.FafApiProperties.PasswordReset;
import com.faforever.api.config.FafApiProperties.Registration;
import com.faforever.api.config.FafApiProperties.UsernameChange;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailService {
  private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+$");
  private final DomainBlacklistRepository domainBlacklistRepository;
  private final FafApiProperties properties;
  private final EmailSender emailSender;

  public EmailService(DomainBlacklistRepository domainBlacklistRepository, FafApiProperties properties, EmailSender emailSender) {
    this.domainBlacklistRepository = domainBlacklistRepository;
    this.properties = properties;
    this.emailSender = emailSender;
  }

  /**
   * Checks whether the specified email address as a valid format and its domain is not blacklisted.
   */
  @Transactional(readOnly = true)
  public void validateEmailAddress(String email) {
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new ApiException(new Error(ErrorCode.EMAIL_INVALID, email));
    }
    if (domainBlacklistRepository.existsByDomain(email.substring(email.lastIndexOf('@') + 1))) {
      throw new ApiException(new Error(ErrorCode.EMAIL_BLACKLISTED, email));
    }
  }

  @SneakyThrows
  public void sendActivationMail(String username, String email, String activationUrl) {
    Registration registration = properties.getRegistration();
    emailSender.sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      registration.getSubject(),
      MessageFormat.format(registration.getHtmlFormat(), username, activationUrl)
    );
  }

  @SneakyThrows
  public void sendUsernameChangeMail(String email, String oldUsername, String newUsername) {
    UsernameChange usernameChange = properties.getUsernameChange();
    emailSender.sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      usernameChange.getMailSubject(),
      MessageFormat.format(usernameChange.getMailBody(), oldUsername, newUsername)
    );
  }


  @SneakyThrows
  public void sendBanMail(String email, String username, String reason, String banner, String createTime, String type, String expires) {
    Ban ban = properties.getBan();
    emailSender.sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      ban.getBanMailSubject(),
      MessageFormat.format(ban.getBanMailBody(), username, reason, banner, createTime, type, expires)
    );
  }

  @SneakyThrows
  public void sendBanRevokeMail(String email, String username, String moderatorThatRevoked, String reasonForRevoke, String reasonForBan, String banner, String banCreateTime) {
    Ban ban = properties.getBan();
    emailSender.sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      ban.getBanRevokeMailSubject(),
      MessageFormat.format(ban.getBanRevokeMailBody(), username, moderatorThatRevoked, reasonForRevoke, reasonForBan, banner, banCreateTime)
    );
  }

  @SneakyThrows
  public void sendPasswordResetMail(String username, String email, String passwordResetUrl) {
    PasswordReset passwordReset = properties.getPasswordReset();
    emailSender.sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      passwordReset.getSubject(),
      MessageFormat.format(passwordReset.getHtmlFormat(), username, passwordResetUrl)
    );
  }
}
