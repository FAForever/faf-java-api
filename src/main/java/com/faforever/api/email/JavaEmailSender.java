package com.faforever.api.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import java.util.Set;

// TODO we might want to use spring mail, but it would've taken too much time when I first looked at it
@Component
@ConditionalOnProperty("spring.mail.host")
public class JavaEmailSender implements EmailSender {

  private final JavaMailSender mailSender;

  public JavaEmailSender(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void sendMail(String fromEmail, String fromName, Set<String> toEmails, String subject, String content) {
    MimeMessagePreparator messagePreparator = mimeMessage -> {
      MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
      messageHelper.setFrom(fromEmail, fromName);
      messageHelper.setTo(toEmails.toArray(new String[]{}));
      messageHelper.setSubject(subject);
      messageHelper.setText(content, true);
    };

    mailSender.send(messagePreparator);
  }
}
