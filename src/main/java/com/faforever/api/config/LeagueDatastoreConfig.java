package com.faforever.api.config;

import com.yahoo.elide.core.datastore.DataStore;
import com.yahoo.elide.datastores.jpa.JpaDataStore;
import com.yahoo.elide.spring.orm.jpa.EntityManagerProxySupplier;
import com.yahoo.elide.spring.orm.jpa.PlatformJpaTransactionSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Configuration
public class LeagueDatastoreConfig {

  public static final String LEAGUE_DATA_SOURCE = "leagueDataSource";
  public static final String LEAGUE_TRANSACTION_MANAGER = "leagueTransactionManager";


  @Bean
  public LocalContainerEntityManagerFactoryBean leagueEntityManagerFactory(
    EntityManagerFactoryBuilder builder,
    @Qualifier(LeagueDatastoreConfig.LEAGUE_DATA_SOURCE) DataSource leagueDataSource
  ) {
    return builder
      .dataSource(leagueDataSource)
      .packages(
        "com.faforever.api.league.domain"
      )
      .build();
  }

  @Bean
  public PlatformTransactionManager leagueTransactionManager(
    @Qualifier("leagueEntityManagerFactory") LocalContainerEntityManagerFactoryBean leagueEntityManagerFactory
  ) {
    return new JpaTransactionManager(leagueEntityManagerFactory.getObject());
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public JpaDataStore.JpaTransactionSupplier leagueJpaTransactionSupplier(
    @Qualifier(LEAGUE_TRANSACTION_MANAGER) PlatformTransactionManager leagueTransactionManager,
    @Qualifier("leagueEntityManagerFactory") EntityManagerFactory entityManagerFactory
  ) {
    return new PlatformJpaTransactionSupplier(
      new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED),
      leagueTransactionManager,
      entityManagerFactory,
     false
    );
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public JpaDataStore.EntityManagerSupplier leagueEntityManagerSupplier() {
    return new EntityManagerProxySupplier();
  }

  @Bean
  DataStore leagueDataStore(
    @Qualifier("leagueJpaTransactionSupplier") JpaDataStore.JpaTransactionSupplier leagueJpaTransactionSupplier,
    @Qualifier("leagueEntityManagerSupplier") JpaDataStore.EntityManagerSupplier leagueEntityManagerSupplier,
    @Qualifier("leagueEntityManagerFactory") EntityManagerFactory entityManagerFactory
  ) {
    return new JpaDataStore(leagueEntityManagerSupplier, leagueJpaTransactionSupplier, entityManagerFactory::getMetamodel);
  }
}
