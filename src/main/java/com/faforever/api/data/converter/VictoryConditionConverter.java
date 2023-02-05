package com.faforever.api.data.converter;

import com.faforever.api.data.domain.VictoryCondition;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

@Converter
public class VictoryConditionConverter implements AttributeConverter<VictoryCondition, String> {
  @Override
  public String convertToDatabaseColumn(VictoryCondition attribute) {
    return String.valueOf(attribute.ordinal());
  }

  @Override
  public VictoryCondition convertToEntityAttribute(String dbData) {
    return Arrays.stream(VictoryCondition.values()).filter(victoryCondition -> String.valueOf(victoryCondition.ordinal()).equals(dbData)).findFirst().orElse(null);
  }
}
