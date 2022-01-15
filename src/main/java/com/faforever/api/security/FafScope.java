package com.faforever.api.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

public record FafScope(@NotNull String scope) implements GrantedAuthority {
  public static final String SCOPE_PREFIX = "SCOPE_";

  public static final String PUBLIC_PROFILE = "public_profile";
  public static final String WRITE_ACHIEVEMENTS = "write_achievements";
  public static final String WRITE_EVENTS = "write_events";
  public static final String UPLOAD_MAP = "upload_map";
  public static final String UPLOAD_MOD = "upload_mod";
  public static final String WRITE_ACCOUNT_DATA = "write_account_data";
  public static final String EDIT_CLAN_DATA = "edit_clan_data";
  public static final String VOTE = "vote";
  public static final String LOBBY = "lobby";
  public static final String READ_SENSIBLE_USERDATA = "read_sensible_userdata";
  public static final String ADMINISTRATIVE_ACTION = "administrative_actions";
  public static final String MANAGE_VAULT = "manage_vault";

  @Override
  public String getAuthority() {
    return SCOPE_PREFIX + scope;
  }

  public boolean matches(String matchingScope) {
    return Objects.equals(scope, matchingScope);
  }
}
