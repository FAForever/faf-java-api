package com.faforever.api.mautic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.io.InputStream;

@Component
public class MauticApiErrorHandler extends DefaultResponseErrorHandler {

  private final ObjectMapper objectMapper;

  public MauticApiErrorHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    try (InputStream inputStream = response.getBody()) {
      MauticErrorResponse errorResponse = objectMapper.readValue(inputStream, MauticErrorResponse.class);
      throw new MauticApiException(errorResponse.getErrors());
    }
  }
}
