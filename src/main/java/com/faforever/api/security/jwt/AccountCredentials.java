package com.faforever.api.security.jwt;

import lombok.Data;

@Data
class AccountCredentials {

  private String username;
  private String password;

}
