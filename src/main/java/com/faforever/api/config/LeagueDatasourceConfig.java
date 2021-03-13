package com.faforever.api.config;

import com.faforever.api.config.elide.SpringHibernateDataStore;
import com.yahoo.elide.core.datastore.DataStore;
import org.hibernate.ScrollMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

@Configuration
public class LeagueDatasourceConfig {

  @Bean
  @ConfigurationProperties("spring.league-datasource")
  public DataSourceProperties leagueDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.league-datasource.configuration")
  public DataSource leagueDataSource() {
    return leagueDataSourceProperties().initializeDataSourceBuilder().build();
  }

  @Bean(name = "leagueEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean leagueEntityManagerFactory(
    EntityManagerFactoryBuilder builder,
    @Qualifier("leagueDataSource") DataSource leagueDataSource
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
    final @Qualifier("leagueEntityManagerFactory") LocalContainerEntityManagerFactoryBean leagueEntityManagerFactory
  ) {
    return new JpaTransactionManager(leagueEntityManagerFactory.getObject());
  }

  @Bean
  DataStore leagueDatastore(
    @Qualifier("leagueTransactionManager") PlatformTransactionManager leagueTransactionManager,
    @Qualifier("leagueEntityManagerFactory") EntityManager entityManager
  ) {
    return new SpringHibernateDataStore(leagueTransactionManager, entityManager, true, ScrollMode.FORWARD_ONLY);
  }
}
