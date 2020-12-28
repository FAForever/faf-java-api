package com.faforever.api.security.elide.permission;

import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;

import static com.faforever.api.security.elide.permission.WriteEmailDomainBanCheck.EXPRESSION;

@SecurityCheck(EXPRESSION)
public class WriteEmailDomainBanCheck extends FafUserCheck {

  public static final String EXPRESSION = "WriteEmailDomainBan";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.ADMINISTRATIVE_ACTION) &&
      checkUserPermission(user, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN);
  }
}
