package com.faforever.api.data.checks;

import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.security.ElideUser;
import com.faforever.api.security.FafUserDetails;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.OperationCheck;

import java.util.Objects;
import java.util.Optional;

public class IsClanMembershipDeletable {
  public static final String EXPRESSION = "IsClanMembershipDeletable";

  public static class Inline extends OperationCheck<ClanMembership> {

    @Override
    public boolean ok(ClanMembership membership, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      final ElideUser caller = (ElideUser) requestScope.getUser();
      final Integer requesterId = caller.getFafUserDetails().map(FafUserDetails::getId).orElse(null);
      return !Objects.equals(membership.getPlayer().getId(), membership.getClan().getLeader().getId())
        && (membership.getClan().getLeader().getId().equals(requesterId) ||
        membership.getPlayer().getId().equals(requesterId));
    }
  }
}
