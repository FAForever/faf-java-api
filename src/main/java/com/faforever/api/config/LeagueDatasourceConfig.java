package com.faforever.api.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Conditional(LeagueDatasourceConfig.NonTestcontainers.class)
@Configuration
public class LeagueDatasourceConfig {

  @Bean
  @ConfigurationProperties("spring.league-datasource")
  @Qualifier("leagueDataSourceProperties")
  public DataSourceProperties leagueDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.league-datasource.configuration")
  public DataSource leagueDataSource(
    @Qualifier("leagueDataSourceProperties") DataSourceProperties leagueDataSourceProperties
  ) {
    return leagueDataSourceProperties.initializeDataSourceBuilder().build();
  }

  static class NonTestcontainers extends NoneNestedConditions {
    public NonTestcontainers() {
      super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(value = "spring.league-datasource.url", havingValue = "jdbc:mariadb://testcontainers/faf-league?useSSL=false")
    static class OnTestcontainers {
    }
  }
}
