package com.faforever.api.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Conditional(FafDatasourceConfig.NonTestcontainers.class)
@Configuration
public class FafDatasourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  @Qualifier("fafDataSourceProperties")
  public DataSourceProperties fafDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource.configuration")
  public DataSource fafDataSource(
    @Qualifier("fafDataSourceProperties") DataSourceProperties fafDataSourceProperties
  ) {
    return fafDataSourceProperties.initializeDataSourceBuilder().build();
  }

  static class NonTestcontainers extends NoneNestedConditions {
    public NonTestcontainers() {
      super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(value = "spring.datasource.url", havingValue = "jdbc:mariadb://testcontainers/faf?useSSL=false")
    static class OnTestcontainers {
    }
  }
}
