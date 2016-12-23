package com.faforever.api.security.oauth2.domain;

import java.util.HashMap;
import java.util.Map;

public enum ClientType {
  CONFIDENTIAL("confidential"), PUBLIC("public");

  private static final Map<String, ClientType> fromString;

  static {
    fromString = new HashMap<>();
    for (ClientType clientType : values()) {
      fromString.put(clientType.string, clientType);
    }
  }

  private final String string;

  ClientType(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }

  public static ClientType fromString(String string) {
    if (!fromString.containsKey(string)) {
      throw new IllegalArgumentException("No such client type: " + string);
    }
    return fromString.get(string);
  }
}
