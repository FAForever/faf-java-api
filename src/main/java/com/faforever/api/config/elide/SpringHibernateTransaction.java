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

import com.yahoo.elide.core.RequestScope;
import com.yahoo.elide.core.exceptions.TransactionException;
import com.yahoo.elide.datastores.hibernate5.HibernateTransaction;
import org.hibernate.ScrollMode;
import org.hibernate.Session;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;

/**
 * Spring Hibernate Transaction.
 *
 * @author olOwOlo
 */
public class SpringHibernateTransaction extends HibernateTransaction {

  private final Session session;
  private final TransactionStatus txStatus;
  private final PlatformTransactionManager txManager;

  /**
   * Constructor.
   *
   * @param session Hibernate session
   * @param txManager Spring PlatformTransactionManager
   * @param txStatus Spring Transaction status
   * @param isScrollEnabled Whether or not scrolling is enabled
   * @param scrollMode Scroll mode to use if scrolling enabled
   */
  protected SpringHibernateTransaction(Session session,
                                       PlatformTransactionManager txManager,
                                       TransactionStatus txStatus,
                                       boolean isScrollEnabled,
                                       ScrollMode scrollMode) {
    super(session, isScrollEnabled, scrollMode);
    this.session = session;
    this.txManager = txManager;
    this.txStatus = txStatus;
  }

  @Override
  public void commit(RequestScope scope) {
    try {
      flush(scope);
      txManager.commit(txStatus);
    } catch (org.springframework.transaction.TransactionException e) {
      throw new TransactionException(e);
    }
  }

  @Override
  public void close() throws IOException {
    if (session.isOpen() && !txStatus.isCompleted()) {
      txManager.rollback(txStatus);
      throw new IOException("Transaction not closed");
    }
  }

}
