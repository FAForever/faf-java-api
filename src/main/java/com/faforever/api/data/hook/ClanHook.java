package com.faforever.api.data.hook;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.core.lifecycle.LifeCycleHook;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClanHook implements LifeCycleHook<Clan> {

  @Override
  public void execute(Operation operation, TransactionPhase phase, Clan clan, RequestScope requestScope, Optional<ChangeSpec> changes) {
    if (operation == Operation.DELETE) {
      clan.getMemberships().stream().map(ClanMembership::getPlayer).forEach(player -> player.setClanMembership(null));
    }
  }
}
