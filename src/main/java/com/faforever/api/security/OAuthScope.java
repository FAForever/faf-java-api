package com.faforever.api.security;

import java.util.HashMap;
import java.util.Map;

public enum OAuthScope {

  PUBLIC_PROFILE("public_profile", "Read your public player data"),
  READ_ACHIEVEMENTS("read_achievements", "Read your achievements"),
  WRITE_ACHIEVEMENTS("write_achievements", "Write your achievements"),
  READ_EVENTS("read_events", "Read events"),
  WRITE_EVENTS("write_events", "Write events"),
  UPLOAD_MAP("upload_map", "Upload maps"),
  UPLOAD_MOD("upload_mod", "Upload mods"),
  WRITE_ACCOUNT_DATA("write_account_data", "Edit account data"),
  EDIT_CLAN_DATA("edit_clan_data", "Edit clan data"),
  UNKNOWN("unknown", "Unknown");

  private static final Map<String, OAuthScope> fromString;

  static {
    fromString = new HashMap<>();
    for (OAuthScope oAuthScope : values()) {
      fromString.put(oAuthScope.key, oAuthScope);
    }
  }

  private String key;
  private String title;

  OAuthScope(String key, String title) {
    this.key = key;
    this.title = title;
  }

  public static OAuthScope fromKey(String key) {
    return fromString.getOrDefault(key, UNKNOWN);
  }

  public String getKey() {
    return key;
  }

  public String getTitle() {
    return title;
  }
}
