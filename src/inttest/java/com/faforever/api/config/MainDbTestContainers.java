package com.faforever.api.config;

import com.faforever.api.AbstractIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@ConditionalOnMissingProperty("spring.datasource.url")
@Configuration
public class MainDbTestContainers {
  private static final MariaDBContainer<?> fafDBContainer = new MariaDBContainer<>("mariadb:10.6");
  private static final GenericContainer<?> flywayMigrationsContainer = new GenericContainer<>("faforever/faf-db-migrations:v122");
  private static final Network sharedNetwork = Network.newNetwork();

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  @Qualifier("fafDataSourceProperties")
  public DataSourceProperties fafDataSourceProperties() {
    final DataSourceProperties dataSourceProperties = new DataSourceProperties();
    dataSourceProperties.setUrl(fafDBContainer.getJdbcUrl());
    dataSourceProperties.setUsername(fafDBContainer.getUsername());
    dataSourceProperties.setPassword(fafDBContainer.getPassword());
    return dataSourceProperties;
  }

  static {
    fafDBContainer
      .withNetwork(sharedNetwork)
      .withNetworkAliases("faf-db")
      .withEnv("MYSQL_ROOT_PASSWORD", "banana")
      .withUsername("faf-java-api")
      .withPassword("banana")
      .withDatabaseName("faf")
      .withReuse(true)
      .start();

    final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);
    Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);

    flywayMigrationsContainer
      .withNetwork(sharedNetwork)
      .withEnv("FLYWAY_URL", "jdbc:mysql://faf-db/faf?useSSL=false")
      .withEnv("FLYWAY_USER", "root")
      .withEnv("FLYWAY_PASSWORD", "banana")
      .withCommand("migrate")
      .withLogConsumer(logConsumer)
      .start();
  }
}
