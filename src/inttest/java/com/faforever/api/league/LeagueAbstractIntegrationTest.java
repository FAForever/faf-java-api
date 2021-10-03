package com.faforever.api.league;

import com.faforever.api.AbstractIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class LeagueAbstractIntegrationTest extends AbstractIntegrationTest {
  private static final MariaDBContainer<?> leagueServiceDBContainer;
  protected static GenericContainer<?> leagueServiceContainer = new GenericContainer<>("faforever/faf-league-service:0.1.2");

  static {
    leagueServiceDBContainer = new MariaDBContainer<>("mariadb:10.6")
      .withUsername("faf-league-service")
      .withPassword("banana")
      .withDatabaseName("faf-league")
      .withReuse(true);
    leagueServiceDBContainer.start();

    final Logger logger = LoggerFactory.getLogger(LeagueAbstractIntegrationTest.class);
    Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);

    leagueServiceContainer
      .withEnv("DB_SERVER", leagueServiceDBContainer.getContainerIpAddress() + ":" + leagueServiceDBContainer.getFirstMappedPort().toString())
      .withEnv("DB_LOGIN", "faf-league-service")
      .withEnv("DB_PASSWORD", "banana")
      .withEnv("DB_NAME", "faf-league")
      .withEnv("AUTO_APPLY_MIGRATIONS", "1")
      .withCommand()
      .withLogConsumer(logConsumer)
      .start();
  }

  @DynamicPropertySource
  public static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
    registry.add("spring.league-datasource.url", leagueServiceDBContainer::getJdbcUrl);
    registry.add("spring.league-datasource.password", leagueServiceDBContainer::getPassword);
    registry.add("spring.league-datasource.username", leagueServiceDBContainer::getUsername);
  }
}
