package com.faforever.api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Slf4j
@Service
public class AuditService {

  private UserSupplier userSupplier;

  public AuditService(UserSupplier userSupplier) {
    this.userSupplier = userSupplier;
  }

  public void logMessage(String message) {
    final FafAuthenticationToken fafAuthenticationToken = userSupplier.get();
    String extendedMessage = MessageFormat.format("{0} [invoked by User ''{1}'' with id ''{2}'']",
      message, fafAuthenticationToken.getUsername(), fafAuthenticationToken.getUserId());
    log.info(extendedMessage);
  }
}
