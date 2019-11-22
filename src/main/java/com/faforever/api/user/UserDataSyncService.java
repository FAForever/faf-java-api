package com.faforever.api.user;

import org.springframework.context.event.EventListener;

/**
 * A service interface to keep tightly coupled SSO-solutions in sync for user data
 */
public interface UserDataSyncService {
  @EventListener
  void userDataChanged(UserUpdatedEvent event);
}
