package com.faforever.api.config.elide.checks;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.user.FafUserDetails;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class IsClanMembershipLeaderButNotLeaderMembership {
  public static final String EXPRESSION = "is clan membership leader but not clan leader";

  public static class Inline extends InlineCheck<ClanMembership> {

    @Override
    public boolean ok(ClanMembership membership, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      Object opaqueUser = requestScope.getUser().getOpaqueUser();
      return opaqueUser instanceof FafUserDetails
          && membership.getClan().getLeader().getId() == ((FafUserDetails) opaqueUser).getId()
          && membership.getPlayer().getId() != membership.getClan().getId();
    }

    @Override
    public boolean ok(com.yahoo.elide.security.User user) {
      return false;
    }
  }
}
