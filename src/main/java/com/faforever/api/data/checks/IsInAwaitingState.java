package com.faforever.api.data.checks;

import com.faforever.api.data.domain.ModerationReport;
import com.faforever.api.data.domain.ModerationReportStatus;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.OperationCheck;

import java.util.Optional;

public class IsInAwaitingState {
  public static final String EXPRESSION = "IsInAwaitingState";

  public static class Inline extends OperationCheck<ModerationReport> {

    @Override
    public boolean ok(ModerationReport moderationReport, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return moderationReport.getReportStatus() == ModerationReportStatus.AWAITING;
    }
  }
}
