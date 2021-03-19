package com.faforever.api.data.converter;

import com.faforever.api.data.domain.VictoryCondition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Converter
public class MapParamsConverter implements AttributeConverter<Map<String, Object>, String> {

  private final ObjectMapper objectMapper;

  public MapParamsConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String convertToDatabaseColumn(Map<String, Object> mapParams) {

    String mapParamsJson = null;
    try {
      mapParamsJson = objectMapper.writeValueAsString(mapParams);
    } catch (final JsonProcessingException e) {
      log.error("JSON writing error", e);
    }

    return mapParamsJson;
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String mapParamsJSON) {

    Map<String, Object> mapParams = null;
    if (mapParamsJSON != null) {
      try {
        mapParams = objectMapper.readValue(mapParamsJSON, Map.class);
      } catch (final IOException e) {
        log.error("JSON reading error", e);
      }
    }

    return mapParams;
  }

}
