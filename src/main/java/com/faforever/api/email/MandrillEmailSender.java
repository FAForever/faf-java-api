package com.faforever.api.email;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@ConditionalOnProperty(value = "faf-api.mail.mandrill-api-key")
public class MandrillEmailSender implements EmailSender {
  private final MandrillApi mandrillApi;

  public MandrillEmailSender(MandrillApi mandrillApi) {
    this.mandrillApi = mandrillApi;
  }

  @Override
  @SneakyThrows
  public void sendMail(String fromEmail, String fromName, Set<String> toEmails, String subject, String content) {
    MandrillMessage message = new MandrillMessage();
    message.setFromEmail(fromEmail);
    message.setFromName(fromName);
    message.setSubject(subject);
    message.setHtml(content);
    message.setAutoText(true);

    final List<Recipient> recipients = toEmails.stream().map(toEmail -> {
      final Recipient recipient = new Recipient();
      recipient.setEmail(toEmail);
      return recipient;
    }).collect(Collectors.toList());
    message.setTo(recipients);

    log.debug("Sending activation email to: {}", toEmails);
    mandrillApi.messages().send(message, false);
  }
}
