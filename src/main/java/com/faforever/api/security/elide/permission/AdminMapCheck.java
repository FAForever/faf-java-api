package com.faforever.api.security.elide.permission;

import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.faforever.api.security.elide.permission.AdminMapCheck.EXPRESSION;

@Slf4j
@SecurityCheck(EXPRESSION)
public class AdminMapCheck extends FafUserCheck {
  public static final String EXPRESSION = "AdminMap";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.MANAGE_VAULT) &&
      checkUserPermission(user, GroupPermission.ROLE_ADMIN_MAP);
  }
}
