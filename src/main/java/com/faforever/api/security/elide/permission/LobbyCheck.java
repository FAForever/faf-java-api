package com.faforever.api.security.elide.permission;

import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.faforever.api.security.elide.permission.LobbyCheck.EXPRESSION;

@Slf4j
@SecurityCheck(EXPRESSION)
public class LobbyCheck extends FafUserCheck {
  public static final String EXPRESSION = "Lobby";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.LOBBY);
  }
}
