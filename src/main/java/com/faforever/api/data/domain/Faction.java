package com.faforever.api.data.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;

public enum Faction {
  // Order is crucial
  AEON("aeon"), CYBRAN("cybran"), UEF("uef"), SERAPHIM("seraphim"), NOMAD("nomad"), UNKNOWN(null);

  private static final java.util.Map<String, Faction> fromString;

  static {
    fromString = new HashMap<>();
    for (Faction faction : values()) {
      fromString.put(faction.string, faction);
    }
  }

  private final String string;

  Faction(String string) {
    this.string = string;
  }

  @JsonCreator
  public static Faction fromFaValue(int value) {
    if (value > 5 || value < 1) {
      return UNKNOWN;
    }
    return Faction.values()[value - 1];
  }

  public static Faction fromString(String string) {
    return fromString.get(string);
  }

  /**
   * Returns the faction value used as in "Forged Alliance Forever".
   */
  @JsonValue
  public int toFaValue() {
    return ordinal() + 1;
  }

  @Converter(autoApply = true)
  public static class FactionConverter implements AttributeConverter<Faction, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Faction attribute) {
      return attribute.toFaValue();
    }

    @Override
    public Faction convertToEntityAttribute(Integer dbData) {
      return fromFaValue(dbData);
    }
  }
}
