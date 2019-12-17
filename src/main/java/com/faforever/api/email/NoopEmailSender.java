package com.faforever.api.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(value = "spring.mail.host", havingValue = "false")
public class NoopEmailSender implements EmailSender, InitializingBean {
  @Override
  public void afterPropertiesSet() {
    log.warn("No email sender defined, using fallback NoopEmailSender instead!");
  }

  @Override
  public void sendMail(String fromEmail, String fromName, String toEmail, String subject, String content) {
    log.debug("Would send email from '{} <{}>' to '{}' with subject '{}' and text: {}",
      fromName, fromEmail, toEmail, subject, content);
  }
}
