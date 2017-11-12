package com.faforever.api.security;

import com.yahoo.elide.audit.AuditLogger;
import com.yahoo.elide.audit.LogMessage;
import com.yahoo.elide.core.RequestScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.text.MessageFormat;

@Slf4j
public class ExtendedAuditLogger extends AuditLogger {
  @Override
  public void commit(RequestScope requestScope) throws IOException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String user;

    if (principal instanceof FafUserDetails) {
      FafUserDetails fafUserDetails = (FafUserDetails) principal;
      user = MessageFormat.format("User `{0}` with id `{1}`", fafUserDetails.getUsername(), fafUserDetails.getId());
    } else {
      user = principal.toString();
    }

    try {
      for (LogMessage message : messages.get()) {
        String extendedMessage = MessageFormat.format("{0} [invoked by {1}]", message.getMessage(), user);
        log.debug(extendedMessage);
      }
    } finally {
      messages.get().clear();
    }
  }
}
