package com.faforever.api.data.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
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
    } catch (final JacksonException e) {
      log.error("Failed to convert Json object {} to String", jsonPayload, e);
    }

    return jsonAsString;
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String jsonAsString) {

    Map<String, Object> jsonPayload = null;
    if (jsonAsString != null) {
      try {
        jsonPayload = objectMapper.readValue(jsonAsString, Map.class);
      } catch (final JacksonException e) {
        log.error("Failed to read stringified Json {}", jsonAsString, e);
      }
    }

    return jsonPayload;
  }

}
