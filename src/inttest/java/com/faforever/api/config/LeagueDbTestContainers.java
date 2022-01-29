package com.faforever.api.config;

import com.faforever.api.AbstractIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

@ConditionalOnMissingProperty("spring.league-datasource.url")
@Configuration
public class LeagueDbTestContainers {
  private static final MariaDBContainer<?> leagueServiceDBContainer = new MariaDBContainer<>("mariadb:10.6");
  private static final GenericContainer<?> leagueServiceContainer = new GenericContainer<>("faforever/faf-league-service:0.1.7");
  private static final Network sharedNetwork = Network.newNetwork();

  @Bean
  @ConfigurationProperties("spring.league-datasource")
  @Qualifier("leagueDataSourceProperties")
  public DataSourceProperties leagueDataSourceProperties() {
    final DataSourceProperties dataSourceProperties = new DataSourceProperties();
    dataSourceProperties.setUrl(leagueServiceDBContainer.getJdbcUrl());
    dataSourceProperties.setUsername(leagueServiceDBContainer.getUsername());
    dataSourceProperties.setPassword(leagueServiceDBContainer.getPassword());
    return dataSourceProperties;
  }

  static {
    leagueServiceDBContainer
      .withNetwork(sharedNetwork)
      .withNetworkAliases("faf-league-db")
      .withUsername("faf-league-service")
      .withPassword("banana")
      .withDatabaseName("faf-league")
      .withReuse(true)
      .start();

    final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);
    Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);

    leagueServiceContainer
      .withNetwork(sharedNetwork)
      .withEnv("DB_SERVER", "faf-league-db")
      .withEnv("DB_LOGIN", "faf-league-service")
      .withEnv("DB_PASSWORD", "banana")
      .withEnv("DB_NAME", "faf-league")
      .withEnv("AUTO_APPLY_MIGRATIONS", "1")
      .withCommand()
      .withLogConsumer(logConsumer)
      .start();
  }
}
