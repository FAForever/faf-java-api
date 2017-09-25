package com.faforever.api.data.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@AllArgsConstructor
@Deprecated
public enum LegacyAccessLevel implements GrantedAuthority {
  ROLE_USER(0),
  ROLE_MODERATOR(1),
  ROLE_ADMINISTRATOR(2);

  private final int code;

  public static LegacyAccessLevel fromCode(int code) {
    for (LegacyAccessLevel level : LegacyAccessLevel.values()) {
      if (level.code == code) {
        return level;
      }
    }

    throw new IllegalArgumentException(String.format("Code '%s' is unknown", code));
  }

  @Override
  public String getAuthority() {
    return this.name();
  }
}
