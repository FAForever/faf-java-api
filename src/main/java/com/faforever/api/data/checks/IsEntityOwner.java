package com.faforever.api.data.checks;

import com.faforever.api.data.domain.OwnableEntity;
import com.faforever.api.security.FafUserDetails;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class IsEntityOwner {

  public static final String EXPRESSION = "is entity owner";

  public static class Inline extends InlineCheck<OwnableEntity> {

    @Override
    public boolean ok(OwnableEntity entity, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      Object opaqueUser = requestScope.getUser().getOpaqueUser();
      return opaqueUser instanceof FafUserDetails
        && entity.getEntityOwner() != null
        && entity.getEntityOwner().getId() == ((FafUserDetails) opaqueUser).getId();
    }

    @Override
    public boolean ok(com.yahoo.elide.security.User user) {
      return false;
    }
  }
}
