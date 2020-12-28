package com.faforever.api.data.checks;

import com.faforever.api.data.domain.UserGroup;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.OperationCheck;

import java.util.Optional;

import static com.faforever.api.data.checks.UserGroupPublicCheck.EXPRESSION;

@SecurityCheck(EXPRESSION)
public class UserGroupPublicCheck extends OperationCheck<UserGroup> {

  public static final String EXPRESSION = "userGroupPublic";

  @Override
  public boolean ok(UserGroup object, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
    return object.isPublic();
  }
}
