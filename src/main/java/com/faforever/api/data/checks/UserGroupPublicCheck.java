package com.faforever.api.data.checks;

import com.faforever.api.data.domain.UserGroup;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.User;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class UserGroupPublicCheck extends InlineCheck<UserGroup> {

  public static final String EXPRESSION = "userGroupPublic";

  @Override
  public boolean ok(UserGroup object, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
    return object.isPublic();
  }

  @Override
  public boolean ok(User user) {
    return false;
  }
}
