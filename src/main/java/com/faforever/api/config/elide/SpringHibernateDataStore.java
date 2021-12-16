/*
 * Copyright (c) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faforever.api.config.elide;

import com.google.common.base.Preconditions;
import com.yahoo.elide.core.datastore.DataStoreTransaction;
import com.yahoo.elide.datastores.jpa.JpaDataStore;
import org.hibernate.Session;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;

public class SpringHibernateDataStore extends JpaDataStore {

  protected final PlatformTransactionManager txManager;
  protected final EntityManager entityManager;
  protected final HibernateTransactionSupplier transactionSupplier;

  public SpringHibernateDataStore(
    PlatformTransactionManager txManager,
    EntityManager entityManager
  ) {
    this(txManager, entityManager, SpringHibernateTransaction::new);
  }

  protected SpringHibernateDataStore(
    PlatformTransactionManager txManager,
    EntityManager entityManager,
    HibernateTransactionSupplier transactionSupplier
  ) {
    super(() -> entityManager, null);
    this.txManager = txManager;
    this.entityManager = entityManager;
    this.transactionSupplier = transactionSupplier;
  }

  @Override
  public DataStoreTransaction beginTransaction() {
    // begin a spring transaction
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setName("elide transaction");
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus txStatus = txManager.getTransaction(def);

    Session session = entityManager.unwrap(Session.class);
    Preconditions.checkNotNull(session);

    return transactionSupplier.get(session, txManager, txStatus);
  }

  @Override
  public DataStoreTransaction beginReadTransaction() {
    // begin a spring transaction
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setName("elide read transaction");
    def.setReadOnly(true);
    TransactionStatus txStatus = txManager.getTransaction(def);

    Session session = entityManager.unwrap(Session.class);
    Preconditions.checkNotNull(session);

    return transactionSupplier.get(session, txManager, txStatus);
  }

  /**
   * Functional interface for describing a method to supply a custom Hibernate transaction.
   */
  @FunctionalInterface
  public interface HibernateTransactionSupplier {
    SpringHibernateTransaction get(Session session, PlatformTransactionManager txManager, TransactionStatus txStatus);
  }
}
