package com.faforever.api.config;

import com.faforever.api.config.elide.SpringHibernateDataStore;
import com.yahoo.elide.core.datastore.DataStore;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.faforever.api",
  entityManagerFactoryRef = "fafEntityManagerFactory",
  transactionManagerRef = "fafTransactionManager"
)
public class FafDatastoreConfig {

  @Primary
  @Bean
  public LocalContainerEntityManagerFactoryBean fafEntityManagerFactory(
    EntityManagerFactoryBuilder builder,
    DefaultListableBeanFactory beanFactory,
    @Qualifier("fafDataSource") DataSource fafDataSource
  ) {
    final LocalContainerEntityManagerFactoryBean emf = builder
      .dataSource(fafDataSource)
      .packages(
        "com.faforever.api.data.domain",
        "com.faforever.api.client",
        "com.faforever.api.db",
        "com.faforever.api.leaderboard",
        "com.faforever.api.featuredmods"
      )
      .build();
    //https://github.com/spring-projects/spring-framework/issues/23968
    emf.getJpaPropertyMap().put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory));
    return emf;
  }

  @Primary
  @Bean
  public PlatformTransactionManager fafTransactionManager(
    @Qualifier("fafEntityManagerFactory") LocalContainerEntityManagerFactoryBean fafEntityManagerFactory
  ) {
    return new JpaTransactionManager(fafEntityManagerFactory.getObject());
  }

  @Bean
  DataStore fafDataStore(
    @Qualifier("fafTransactionManager") PlatformTransactionManager fafTransactionManager,
    @Qualifier("fafEntityManagerFactory") EntityManager entityManager
  ) {
    return new SpringHibernateDataStore(fafTransactionManager, entityManager);
  }
}
