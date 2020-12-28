package com.faforever.api.security.elide.permission;

import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.faforever.api.security.elide.permission.AdminAccountBanCheck.EXPRESSION;

@Slf4j
@SecurityCheck(EXPRESSION)
public class AdminAccountBanCheck extends FafUserCheck {
  public static final String EXPRESSION = "AdminAccountBan";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.ADMINISTRATIVE_ACTION) &&
      checkUserPermission(user, GroupPermission.ROLE_ADMIN_ACCOUNT_BAN);
  }
}
