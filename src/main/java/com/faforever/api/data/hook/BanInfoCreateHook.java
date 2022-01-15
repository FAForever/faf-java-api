package com.faforever.api.data.hook;

import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.Player;
import com.faforever.api.security.ElideUser;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.core.lifecycle.LifeCycleHook;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;

import java.util.Optional;

public class BanInfoCreateHook implements LifeCycleHook<BanInfo> {
  @Override
  public void execute(Operation operation, TransactionPhase phase, BanInfo banInfo, RequestScope requestScope, Optional<ChangeSpec> changes) {
    final ElideUser caller = (ElideUser) requestScope.getUser();
    caller.getFafId().ifPresent(fafId -> {
      final Player callerPlayer = new Player();
      callerPlayer.setId(fafId);
      banInfo.setAuthor(callerPlayer);
    });
  }
}
