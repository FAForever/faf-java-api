package com.faforever.api.email;

import com.faforever.api.email.NoopEmailSender.MailSenderCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@Conditional(MailSenderCondition.class)
public class NoopEmailSender implements EmailSender {
  @Override
  public void sendMail(String fromEmail, String fromName, Set<String> toEmails, String subject, String content) {
    log.debug("Would send email from '{} <{}>' to '{}' with subject '{}' and text: {}",
      fromName, fromEmail, toEmails, subject, content);
  }

  static class MailSenderCondition extends NoneNestedConditions {

    MailSenderCondition() {
      super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty("faf-api.mail.mandrill-api-key")
    static class HostProperty {

    }

    @ConditionalOnProperty("spring.mail.host")
    static class JndiNameProperty {

    }
  }
}
