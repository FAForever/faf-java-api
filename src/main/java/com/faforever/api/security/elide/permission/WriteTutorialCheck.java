package com.faforever.api.security.elide.permission;

import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;

import static com.faforever.api.security.elide.permission.WriteTutorialCheck.EXPRESSION;

@SecurityCheck(EXPRESSION)
public class WriteTutorialCheck extends FafUserCheck {

  public static final String EXPRESSION = "WriteTutorial";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.ADMINISTRATIVE_ACTION) &&
      checkUserPermission(user, GroupPermission.ROLE_WRITE_TUTORIAL);
  }
}
