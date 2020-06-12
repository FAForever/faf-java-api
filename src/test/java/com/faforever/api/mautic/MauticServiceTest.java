package com.faforever.api.mautic;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MauticServiceTest {

  private MauticService instance;

  @Mock
  private MappingJackson2HttpMessageConverter messageConverter;
  @Mock
  private ResponseErrorHandler mauticApiErrorHandler;
  @Mock
  private RestTemplateBuilder restTemplateBuilder;
  @Mock
  private RestTemplate restTemplate;

  @BeforeEach
  public void setUp() {
    when(restTemplateBuilder.build()).thenReturn(restTemplate);

    when(restTemplateBuilder.additionalMessageConverters(messageConverter)).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.errorHandler(mauticApiErrorHandler)).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.rootUri(any())).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.basicAuthentication(any(), any())).thenReturn(restTemplateBuilder);

    instance = new MauticService(messageConverter, mauticApiErrorHandler, new FafApiProperties(), restTemplateBuilder);
  }

  @Test
  public void userDataChanged() {
    instance.userDataChanged(new UserUpdatedEvent(
      mock(User.class),
      111,
      "JUnit",
      "junit@example.com",
      "1.1.1.1"
    ));

    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(restTemplate).postForObject(eq("/contacts/new"), captor.capture(), eq(Object.class));
  }
}
