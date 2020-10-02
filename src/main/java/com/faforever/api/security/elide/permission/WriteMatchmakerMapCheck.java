package com.faforever.api.security.elide.permission;

import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.security.User;

public class WriteMatchmakerMapCheck extends FafUserCheck {

  public static final String EXPRESSION = "AdminMatchmakerMap";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.ADMINISTRATIVE_ACTION) &&
      checkUserPermission(user, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP);
  }
}
