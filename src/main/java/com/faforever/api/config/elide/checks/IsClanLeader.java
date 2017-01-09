package com.faforever.api.config.elide.checks;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.user.FafUserDetails;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class IsClanLeader {

  public static class Inline extends InlineCheck<Clan> {

    @Override
    public boolean ok(Clan clan, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      Object opaqueUser = requestScope.getUser().getOpaqueUser();
      return opaqueUser instanceof FafUserDetails
          && clan.getLeader().getId() == ((FafUserDetails) opaqueUser).getId();
    }

    @Override
    public boolean ok(com.yahoo.elide.security.User user) {
      return false;
    }
  }
}
