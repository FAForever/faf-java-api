package com.faforever.api.config;

import com.yahoo.elide.datastores.hibernate5.AbstractHibernateStore;
import org.hibernate.jpa.HibernateEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.persistence.EntityManager;

@Configuration
@Profile(ApplicationProfile.INTEGRATION_TEST)
public class ElideTestConfig {
  @Bean
  AbstractHibernateStore hibernateStore(EntityManager entityManager) {
    return new AbstractHibernateStore.Builder((HibernateEntityManager) entityManager).build();
  }
}
