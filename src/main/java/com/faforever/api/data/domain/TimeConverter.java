package com.faforever.api.data.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Time;
import java.util.Optional;

@Converter
public class TimeConverter implements AttributeConverter<Long, Time> {
  @Override
  public Time convertToDatabaseColumn(Long seconds) {
    return Optional.ofNullable(seconds)
      .map(aLong -> new Time(seconds * 1000))
      .orElse(null);
  }

  @Override
  public Long convertToEntityAttribute(Time dbData) {
    return Optional.ofNullable(dbData)
      .map(time -> time.getTime() / 1000)
      .orElse(null);
  }
}
