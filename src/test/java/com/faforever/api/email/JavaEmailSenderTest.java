package com.faforever.api.email;

import com.sun.mail.smtp.SMTPMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JavaEmailSenderTest {

  private JavaEmailSender instance;

  @Mock
  private JavaMailSender mailSender;

  @Before
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
