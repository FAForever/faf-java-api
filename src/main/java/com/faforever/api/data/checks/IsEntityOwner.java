package com.faforever.api.data.checks;

import com.faforever.api.data.domain.OwnableEntity;
import com.faforever.api.security.ElideUser;
import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.OperationCheck;

import java.util.Optional;

/**
 * Operation check that will check if the entity's owner is callee
 * For entity's read filtration use {@link IsEntityOwnerFilter}
 */
public class IsEntityOwner {

  public static final String EXPRESSION = "is entity owner";

  @SecurityCheck(EXPRESSION)
  public static class Inline extends OperationCheck<OwnableEntity> {

    @Override
    public boolean ok(OwnableEntity entity, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      final ElideUser caller = (ElideUser) requestScope.getUser();
      return entity.getEntityOwner() != null
        && entity.getEntityOwner().getId().equals(caller.getFafId().orElse(null));
    }
  }
}
