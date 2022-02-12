package com.faforever.api.email;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
  private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+$");
  private final DomainBlacklistRepository domainBlacklistRepository;
  private final FafApiProperties properties;
  private final EmailSender emailSender;
  private final MailBodyBuilder mailBodyBuilder;

  /**
   * Checks whether the specified email address as a valid format and its domain is not blacklisted.
   */
  @Transactional(readOnly = true)
  public void validateEmailAddress(String email) {
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw ApiException.of(ErrorCode.EMAIL_INVALID, email);
    }
    if (domainBlacklistRepository.existsByDomain(email.substring(email.lastIndexOf('@') + 1))) {
      throw ApiException.of(ErrorCode.EMAIL_BLACKLISTED, email);
    }
  }

  public void sendActivationMail(String username, String email, String activationUrl) throws IOException {
    final var mailBody = mailBodyBuilder.buildAccountActivationBody(username, activationUrl);

    emailSender.sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      properties.getRegistration().getSubject(),
      mailBody
    );
  }

  public void sendPasswordResetMail(String username, String email, String passwordResetUrl) throws IOException {
    final var mailBody = mailBodyBuilder.buildPasswordResetBody(username, passwordResetUrl);

    emailSender.sendMail(
      properties.getMail().getFromEmailAddress(),
      properties.getMail().getFromEmailName(),
      email,
      properties.getPasswordReset().getSubject(),
      mailBody
    );
  }
}
