package com.faforever.api.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum OAuthScope {

  PUBLIC_PROFILE(OAuthScope._PUBLIC_PROFILE, "Read your public account data"),
  CREATE_USER(OAuthScope._CREATE_USER, "Create users"),
  READ_ACHIEVEMENTS(OAuthScope._READ_ACHIEVEMENTS, "Read your achievements"),
  WRITE_ACHIEVEMENTS(OAuthScope._WRITE_ACHIEVEMENTS, "Write your achievements"),
  READ_EVENTS(OAuthScope._READ_EVENTS, "Read events"),
  WRITE_EVENTS(OAuthScope._WRITE_EVENTS, "Write events"),
  UPLOAD_MAP(OAuthScope._UPLOAD_MAP, "Upload maps"),
  UPLOAD_MOD(OAuthScope._UPLOAD_MOD, "Upload mods"),
  UPLOAD_AVATAR(OAuthScope._UPLOAD_AVATAR, "Upload avatars"),
  WRITE_ACCOUNT_DATA(OAuthScope._WRITE_ACCOUNT_DATA, "Edit account data"),
  EDIT_CLAN_DATA(OAuthScope._EDIT_CLAN_DATA, "Edit clan data"),
  VOTE(OAuthScope._VOTE, "Vote"),
  READ_SENSIBLE_USERDATA(OAuthScope._READ_SENSIBLE_USERDATA, "View sensible user data (email addresses, ip addresses, etc.)"),
  ADMINISTRATIVE_ACTION(OAuthScope._ADMINISTRATIVE_ACTION, "Administrative actions"),
  MANAGE_VAULT(OAuthScope._MANAGE_VAULT, "Manage vault");

  public static final String _PUBLIC_PROFILE = "public_profile";
  public static final String _CREATE_USER = "create_user";
  public static final String _READ_ACHIEVEMENTS = "read_achievements";
  public static final String _WRITE_ACHIEVEMENTS = "write_achievements";
  public static final String _READ_EVENTS = "read_events";
  public static final String _WRITE_EVENTS = "write_events";
  public static final String _UPLOAD_MAP = "upload_map";
  public static final String _UPLOAD_MOD = "upload_mod";
  public static final String _UPLOAD_AVATAR = "upload_avatar";
  public static final String _WRITE_ACCOUNT_DATA = "write_account_data";
  public static final String _EDIT_CLAN_DATA = "edit_clan_data";
  public static final String _VOTE = "vote";
  public static final String _READ_SENSIBLE_USERDATA = "read_sensible_userdata";
  public static final String _ADMINISTRATIVE_ACTION = "administrative_actions";
  public static final String _MANAGE_VAULT = "manage_vault";

  private static final Map<String, OAuthScope> fromString;

  static {
    fromString = new HashMap<>();
    for (OAuthScope oAuthScope : values()) {
      fromString.put(oAuthScope.key, oAuthScope);
    }
  }

  private final String key;
  private final String title;

  OAuthScope(String key, String title) {
    this.key = key;
    this.title = title;
  }

  public static Optional<OAuthScope> fromKey(String key) {
    return Optional.ofNullable(fromString.get(key));
  }

  public String getKey() {
    return key;
  }

  public String getTitle() {
    return title;
  }
}
