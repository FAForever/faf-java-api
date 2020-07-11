package com.faforever.api;

import com.faforever.api.config.FafApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties({FafApiProperties.class})
public class FafApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(FafApiApplication.class, args);
  }
}
