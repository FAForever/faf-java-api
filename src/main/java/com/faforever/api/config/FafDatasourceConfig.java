package com.faforever.api.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@ConditionalOnProperty("spring.datasource.url")
@Configuration
public class FafDatasourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  @Qualifier("fafDataSourceProperties")
  public DataSourceProperties fafDataSourceProperties() {
    return new DataSourceProperties();
  }
}
