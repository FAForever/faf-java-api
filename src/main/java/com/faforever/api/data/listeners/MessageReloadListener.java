package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Message;
import com.faforever.api.i18n.RepositoryMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.inject.Inject;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

@Component
public class MessageReloadListener {
  private static RepositoryMessageSource repositoryMessageSource;

  @Inject
  public void init(RepositoryMessageSource repositoryMessageSource) {
    MessageReloadListener.repositoryMessageSource = repositoryMessageSource;
  }

  @PostPersist
  @PostRemove
  @PostUpdate
  public void reload(Message message) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
      public void afterCommit() {
        repositoryMessageSource.afterPropertiesSet();
      }
    });
  }
}
