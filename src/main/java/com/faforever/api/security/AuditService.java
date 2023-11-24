package com.faforever.api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Slf4j
@Service
public class AuditService {

  private final UserSupplier userSupplier;

  public AuditService(UserSupplier userSupplier) {
    this.userSupplier = userSupplier;
  }

  public void logMessage(String message) {
    final String extendedMessage = userSupplier.get()
      .map(fafAuthenticationToken -> {
        //move to switch pattern matching with java 21
        if (fafAuthenticationToken instanceof FafUserAuthenticationToken fafUserAuthenticationToken) {
          return MessageFormat.format("{0} [invoked by User ''{1}'' with id ''{2}'']",
            message, fafUserAuthenticationToken.getUsername(), fafUserAuthenticationToken.getUserId());
        } else if (fafAuthenticationToken instanceof FafServiceAuthenticationToken fafServiceAuthenticationToken) {
          return MessageFormat.format("{0} [invoked by Service ''{1}'']",
            message, fafServiceAuthenticationToken.getServiceName());
        } else {
          throw new RuntimeException();
        }
      })
      .orElseGet(() -> MessageFormat.format("{0} [invoked by Annonymous user]", message));
    log.info(extendedMessage);
  }
}
