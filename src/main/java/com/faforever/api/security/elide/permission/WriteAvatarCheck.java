package com.faforever.api.security.elide.permission;

import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;

import static com.faforever.api.security.elide.permission.WriteAvatarCheck.EXPRESSION;

@SecurityCheck(EXPRESSION)
public class WriteAvatarCheck extends FafUserCheck {

  public static final String EXPRESSION = "WriteAvatar";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.ADMINISTRATIVE_ACTION) &&
      checkUserPermission(user, GroupPermission.ROLE_WRITE_AVATAR);
  }
}
