package com.faforever.api.league;

import com.faforever.api.AbstractIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class LeagueAbstractIntegrationTest extends AbstractIntegrationTest {
  private static final MySQLContainer mySQLContainer;
  protected static GenericContainer<?> leagueServiceContainer = new GenericContainer<>("faforever/faf-league-service:0.1.2");

  static {
    mySQLContainer = (MySQLContainer) (new MySQLContainer("mysql:5.7")
      .withUsername("faf-league-service")
      .withPassword("banana")
      .withDatabaseName("faf-league")
      .withReuse(true));
    mySQLContainer.start();

    leagueServiceContainer
      .withEnv("DB_SERVER", mySQLContainer.getContainerIpAddress() + ":" + mySQLContainer.getFirstMappedPort().toString())
      .withEnv("DB_LOGIN", "faf-league-service")
      .withEnv("DB_PASSWORD", "banana")
      .withEnv("DB_NAME", "faf-league")
      .withEnv("AUTO_APPLY_MIGRATIONS", "1")
      .withCommand();
    final Logger logger = LoggerFactory.getLogger(LeagueAbstractIntegrationTest.class);
    Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
    leagueServiceContainer.withLogConsumer(logConsumer);
    leagueServiceContainer.start();
  }

  @DynamicPropertySource
  public static void setDatasourceProperties(final DynamicPropertyRegistry registry) {
    registry.add("spring.league-datasource.url", mySQLContainer::getJdbcUrl);
    registry.add("spring.league-datasource.password", mySQLContainer::getPassword);
    registry.add("spring.league-datasource.username", mySQLContainer::getUsername);
  }
}
