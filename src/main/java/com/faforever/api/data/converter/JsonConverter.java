package com.faforever.api.data.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Converter
@RequiredArgsConstructor
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

  private final ObjectMapper objectMapper;

  @Override
  public String convertToDatabaseColumn(Map<String, Object> jsonPayload) {

    String jsonAsString = null;
    try {
      jsonAsString = objectMapper.writeValueAsString(jsonPayload);
    } catch (final JsonProcessingException e) {
      log.error("Failed to convert Json object to String", e);
    }

    return jsonAsString;
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String jsonAsString) {

    Map<String, Object> jsonPayload = null;
    if (jsonAsString != null) {
      try {
        jsonPayload = objectMapper.readValue(jsonAsString, Map.class);
      } catch (final IOException e) {
        log.error("Failed to read stringified Json", e);
      }
    }

    return jsonPayload;
  }

}
