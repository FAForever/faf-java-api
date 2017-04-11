package com.faforever.api.email;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
@ConditionalOnProperty(value = "faf-server.mail.mandrill-api-key")
public class MandrillEmailSender implements EmailSender {
  private final MandrillApi mandrillApi;

  public MandrillEmailSender(MandrillApi mandrillApi) {
    this.mandrillApi = mandrillApi;
  }

  @Override
  @SneakyThrows
  public void sendMail(String fromEmail, String fromName, String toEmail, String subject, String content) {
    MandrillMessage message = new MandrillMessage();
    message.setFromEmail(fromEmail);
    message.setFromName(fromName);
    message.setSubject(subject);
    message.setHtml(content);
    message.setAutoText(true);

    Recipient recipient = new Recipient();
    recipient.setEmail(toEmail);
    message.setTo(Collections.singletonList(recipient));

    log.debug("Sending activation email to: {}", toEmail);
    mandrillApi.messages().send(message, false);
  }
}
