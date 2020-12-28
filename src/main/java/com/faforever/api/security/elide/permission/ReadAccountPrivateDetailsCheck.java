package com.faforever.api.security.elide.permission;

import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.User;
import lombok.extern.slf4j.Slf4j;

import static com.faforever.api.security.elide.permission.ReadAccountPrivateDetailsCheck.EXPRESSION;

@Slf4j
@SecurityCheck(EXPRESSION)
public class ReadAccountPrivateDetailsCheck extends FafUserCheck {
  public static final String EXPRESSION = "ReadAccountPrivateDetails";

  @Override
  public boolean ok(User user) {
    return checkOAuthScopes(OAuthScope.READ_SENSIBLE_USERDATA) &&
      checkUserPermission(user, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS);
  }
}
