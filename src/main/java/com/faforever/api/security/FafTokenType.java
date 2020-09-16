package com.faforever.api.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FafTokenType {
  REGISTRATION,
  PASSWORD_RESET,
  LINK_TO_STEAM,
  PASSWORD_RESET_STEAM
}
