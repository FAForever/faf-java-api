package com.faforever.api.data.hook;

import com.faforever.api.data.domain.ModerationReport;
import com.faforever.api.data.domain.ModerationReportStatus;
import com.faforever.api.data.domain.Player;
import com.faforever.api.security.ElideUser;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.core.lifecycle.LifeCycleHook;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@Component
public class ModerationReportHook implements LifeCycleHook<ModerationReport> {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public void execute(Operation operation, TransactionPhase phase, ModerationReport moderationReport, RequestScope requestScope, Optional<ChangeSpec> changes) {
    final ElideUser caller = (ElideUser) requestScope.getUser();
    final Player callerPlayer = caller.getFafUserId().map(fafPlayerId ->
      entityManager.getReference(Player.class, fafPlayerId)
    ).orElse(null);
    if (operation == Operation.CREATE) {
      moderationReport.setReportStatus(ModerationReportStatus.AWAITING);
      moderationReport.setReporter(callerPlayer);
    } else if (operation == Operation.UPDATE) {
      moderationReport.setLastModerator(callerPlayer);
    }
  }
}
