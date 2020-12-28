package com.faforever.api.data.checks;

import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.OperationCheck;

import java.util.Optional;

public class BooleanChange {

  public static final String TO_FALSE_EXPRESSION = "boolean changed to false";
  public static final String TO_TRUE_EXPRESSION = "boolean changed to true";


  public static class ToFalse extends OperationCheck<Object> {

    @Override
    public boolean ok(Object entity, RequestScope requestScope, Optional<ChangeSpec> optionalChangeSpec) {
      if (!optionalChangeSpec.isPresent()) {
        return true;
      }
      ChangeSpec changeSpec = optionalChangeSpec.get();
      if (!(changeSpec.getModified() instanceof Boolean)) {
        throw new IllegalStateException("This expression can only be applied to boolean fields");
      }
      return !((Boolean) changeSpec.getModified());
    }

  }

  public static class ToTrue extends OperationCheck<Object> {

    @Override
    public boolean ok(Object entity, RequestScope requestScope, Optional<ChangeSpec> optionalChangeSpec) {
      if (!optionalChangeSpec.isPresent()) {
        return true;
      }
      ChangeSpec changeSpec = optionalChangeSpec.get();
      if (!(changeSpec.getModified() instanceof Boolean)) {
        throw new IllegalStateException("This expression can only be applied to boolean fields");
      }
      return ((Boolean) changeSpec.getModified());
    }

  }
}
