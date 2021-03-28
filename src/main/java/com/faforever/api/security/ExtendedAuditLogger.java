package com.faforever.api.security;

import com.yahoo.elide.core.audit.AuditLogger;
import com.yahoo.elide.core.audit.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExtendedAuditLogger extends AuditLogger {
  private AuditService auditService;

  public ExtendedAuditLogger(AuditService auditService) {
    this.auditService = auditService;
  }

  @Override
  public void commit() {
    try {
      for (LogMessage message : MESSAGES.get()) {
        auditService.logMessage(message.getMessage());
      }
    } finally {
      MESSAGES.get().clear();
    }
  }
}
