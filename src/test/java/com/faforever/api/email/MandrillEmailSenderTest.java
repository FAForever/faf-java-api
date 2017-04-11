package com.faforever.api.email;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.controller.MandrillMessagesApi;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MandrillEmailSenderTest {
  private MandrillEmailSender instance;

  @Mock
  private MandrillApi mandrillApi;

  @Before
  public void setUp() throws Exception {
    instance = new MandrillEmailSender(mandrillApi);
  }

  @Test
  public void sendMail() throws Exception {
    MandrillMessagesApi messages = mock(MandrillMessagesApi.class);
    when(mandrillApi.messages()).thenReturn(messages);

    instance.sendMail("faf@example.com", "FAF", "junit@example.com", "test", "foo");

    ArgumentCaptor<MandrillMessage> captor = ArgumentCaptor.forClass(MandrillMessage.class);
    verify(messages).send(captor.capture(), eq(false));

    MandrillMessage value = captor.getValue();
    assertThat(value.getAutoText(), is(true));
    assertThat(value.getFromEmail(), is("faf@example.com"));
    assertThat(value.getFromName(), is("FAF"));
    assertThat(value.getSubject(), is("test"));
    assertThat(value.getHtml(), is("foo"));
    assertThat(value.getTo(), hasSize(1));
    assertThat(value.getTo().get(0).getEmail(), is("junit@example.com"));
  }
}
