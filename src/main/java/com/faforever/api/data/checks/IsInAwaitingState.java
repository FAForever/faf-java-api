package com.faforever.api.data.checks;

import com.faforever.api.data.domain.ModerationReport;
import com.faforever.api.data.domain.ModerationReportStatus;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.InlineCheck;

import java.util.Optional;

public class IsInAwaitingState {
  public static final String EXPRESSION = "IsInAwaitingState";

  public static class Inline extends InlineCheck<ModerationReport> {

    @Override
    public boolean ok(ModerationReport moderationReport, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return moderationReport.getReportStatus() == ModerationReportStatus.AWAITING;
    }

    @Override
    public boolean ok(com.yahoo.elide.security.User user) {
      return false;
    }
  }
}
