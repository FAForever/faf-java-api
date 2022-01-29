package com.faforever.api.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty("spring.league-datasource.url")
@Configuration
public class LeagueDatasourceConfig {

  @Bean
  @ConfigurationProperties("spring.league-datasource")
  @Qualifier("leagueDataSourceProperties")
  public DataSourceProperties leagueDataSourceProperties() {
    return new DataSourceProperties();
  }
}
