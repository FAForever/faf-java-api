package com.faforever.api.mautic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class MauticApiErrorHandler extends DefaultResponseErrorHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handleError(@NotNull ClientHttpResponse response) throws IOException {
    try (InputStream inputStream = response.getBody()) {
      MauticErrorResponse errorResponse = objectMapper.readValue(inputStream, MauticErrorResponse.class);
      throw new MauticApiException(errorResponse.errors());
    }
  }
}
