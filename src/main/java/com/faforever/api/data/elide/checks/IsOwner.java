package com.faforever.api.data.elide.checks;

import com.faforever.api.data.domain.Player;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.User;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class IsOwner {

  public static class Inline extends InlineCheck<Player> {

    @Override
    public boolean ok(Player player, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return player.getId().equals(requestScope.getUser().getOpaqueUser());
    }

    @Override
    public boolean ok(User user) {
      return false;
    }
  }
}
