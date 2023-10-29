package com.faforever.api.config;

import com.yahoo.elide.core.datastore.DataStore;
import com.yahoo.elide.datastores.jpa.JpaDataStore;
import com.yahoo.elide.spring.orm.jpa.EntityManagerProxySupplier;
import com.yahoo.elide.spring.orm.jpa.PlatformJpaTransactionSupplier;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

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
  @Scope(SCOPE_PROTOTYPE)
  public JpaDataStore.JpaTransactionSupplier fafJpaTransactionSupplier(
    @Qualifier("fafTransactionManager") PlatformTransactionManager fafTransactionManager,
    @Qualifier("fafEntityManagerFactory") EntityManagerFactory entityManagerFactory
  ) {
    return new PlatformJpaTransactionSupplier(
      new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED),
      fafTransactionManager,
      entityManagerFactory,
      false
    );
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public JpaDataStore.EntityManagerSupplier fafEntityManagerSupplier() {
    return new EntityManagerProxySupplier();
  }

  @Bean
  DataStore fafDataStore(
    @Qualifier("fafJpaTransactionSupplier") JpaDataStore.JpaTransactionSupplier fafJpaTransactionSupplier,
    @Qualifier("fafEntityManagerSupplier") JpaDataStore.EntityManagerSupplier fafEntityManagerSupplier,
    @Qualifier("fafEntityManagerFactory") EntityManagerFactory entityManagerFactory
  ) {
    return new JpaDataStore(fafEntityManagerSupplier, fafJpaTransactionSupplier, entityManagerFactory::getMetamodel);
  }
}
