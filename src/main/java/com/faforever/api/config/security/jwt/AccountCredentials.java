package com.faforever.api.config.security.jwt;

import lombok.Data;

@Data
class AccountCredentials {

  private String username;
  private String password;

}
