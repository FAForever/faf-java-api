package com.faforever.api.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(value = "faf-api.mail.mandrill-api-key", havingValue = "false", matchIfMissing = true)
public class NoopEmailSender implements EmailSender {
  @Override
  public void sendMail(String fromEmail, String fromName, String toEmail, String subject, String content) {
    log.debug("Sending email from '{} <{}>' to '{}' with subject '{}' and text: {}",
      fromName, fromEmail, toEmail, subject, content);
  }
}
