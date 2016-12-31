package com.faforever.api.config.elide.checks;

import com.faforever.api.data.domain.Login;
import com.faforever.api.user.FafUserDetails;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class IsOwner {

  public static class Inline extends InlineCheck<Login> {

    @Override
    public boolean ok(Login login, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      Object opaqueUser = requestScope.getUser().getOpaqueUser();
      return opaqueUser instanceof FafUserDetails
          && login.getId() == ((FafUserDetails) opaqueUser).getId();
    }

    @Override
    public boolean ok(com.yahoo.elide.security.User user) {
      return false;
    }
  }
}
