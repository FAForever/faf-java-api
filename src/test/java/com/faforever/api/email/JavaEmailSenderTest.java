package com.faforever.api.email;

import org.eclipse.angus.mail.smtp.SMTPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JavaEmailSenderTest {

  private JavaEmailSender instance;

  @Mock
  private JavaMailSender mailSender;

  @BeforeEach
  public void setUp() {
    instance = new JavaEmailSender(mailSender);
  }

  @Test
  public void sendMail() throws Exception {
    instance.sendMail("fromEmail", "fromName", "toEmail", "subject", "content");

    ArgumentCaptor<MimeMessagePreparator> captor = ArgumentCaptor.forClass(MimeMessagePreparator.class);
    verify(mailSender).send(captor.capture());

    MimeMessage mimeMessage = new SMTPMessage((Session) null);
    captor.getValue().prepare(mimeMessage);

    assertThat(mimeMessage.getAllRecipients()[0], is(new InternetAddress("toEmail")));
    assertThat(mimeMessage.getFrom()[0], is(new InternetAddress("fromEmail", "fromName")));
    assertThat(mimeMessage.getSubject(), is("subject"));
    assertThat(mimeMessage.getContent(), is("content"));
  }
}
