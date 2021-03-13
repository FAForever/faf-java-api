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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.faforever.api",
  entityManagerFactoryRef = "fafEntityManagerFactory",
  transactionManagerRef = "fafTransactionManager"
)
public class FafDatasourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties fafDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource.configuration")
  public DataSource fafDataSource() {
    return fafDataSourceProperties().initializeDataSourceBuilder().build();
  }

  @Primary
  @Bean(name = "fafEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean fafEntityManagerFactory(
    EntityManagerFactoryBuilder builder,
    @Qualifier("fafDataSource") DataSource fafDataSource
  ) {
    return builder
      .dataSource(fafDataSource)
      .packages(
        "com.faforever.api.data.domain",
        "com.faforever.api.client",
        "com.faforever.api.db",
        "com.faforever.api.leaderboard",
        "com.faforever.api.featuredmods"
      )
      .build();
  }

  @Primary
  @Bean
  public PlatformTransactionManager fafTransactionManager(
    final @Qualifier("fafEntityManagerFactory") LocalContainerEntityManagerFactoryBean fafEntityManagerFactory
  ) {
    return new JpaTransactionManager(fafEntityManagerFactory.getObject());
  }

  @Bean
  DataStore fafDatastore(
    @Qualifier("fafTransactionManager") PlatformTransactionManager fafTransactionManager,
    @Qualifier("fafEntityManagerFactory") EntityManager entityManager
  ) {
    return new SpringHibernateDataStore(fafTransactionManager, entityManager, true, ScrollMode.FORWARD_ONLY);
  }
}
