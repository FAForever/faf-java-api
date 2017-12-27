package com.faforever.api.security;

import com.yahoo.elide.audit.AuditLogger;
import com.yahoo.elide.audit.LogMessage;
import com.yahoo.elide.core.RequestScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ExtendedAuditLogger extends AuditLogger {
  private AuditService auditService;

  public ExtendedAuditLogger(AuditService auditService) {
    this.auditService = auditService;
  }

  @Override
  public void commit(RequestScope requestScope) throws IOException {
    try {
      for (LogMessage message : messages.get()) {
        auditService.logMessage(message.getMessage());
      }
    } finally {
      messages.get().clear();
    }
  }
}
