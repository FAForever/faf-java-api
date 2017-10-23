package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.PasswordReset;
import com.faforever.api.config.FafApiProperties.Registration;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        registration.getFromEmail(),
        registration.getFromName(),
        email,
        registration.getSubject(),
        String.format(registration.getHtmlFormat(), username, activationUrl)
    );
  }

  @SneakyThrows
  public void sendPasswordResetMail(String username, String email, String passwordResetUrl) {
    PasswordReset passwordReset = properties.getPasswordReset();
    emailSender.sendMail(
        passwordReset.getFromEmail(),
        passwordReset.getFromName(),
        email,
        passwordReset.getSubject(),
        String.format(passwordReset.getHtmlFormat(), username, passwordResetUrl)
    );
  }
}
