package com.faforever.api.mautic;

import com.faforever.api.config.FafApiProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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

  @Before
  public void setUp() {
    when(restTemplateBuilder.build()).thenReturn(restTemplate);

    when(restTemplateBuilder.additionalMessageConverters(messageConverter)).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.errorHandler(mauticApiErrorHandler)).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.rootUri(any())).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.basicAuthorization(any(), any())).thenReturn(restTemplateBuilder);

    instance = new MauticService(messageConverter, mauticApiErrorHandler, new FafApiProperties(), restTemplateBuilder);
  }

  @Test
  public void createOrUpdateContact() {
    instance.createOrUpdateContact("junit@example.com", "111", "JUnit", "1.1.1.1", OffsetDateTime.now());

    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(restTemplate).postForObject(eq("/contacts/new"), captor.capture(), eq(Object.class));
  }
}
