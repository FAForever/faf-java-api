package com.faforever.api;

import com.faforever.api.config.FafApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.OAuth2AuthorizationServerConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.method.OAuth2MethodSecurityConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {OAuth2AutoConfiguration.class})
@Import({OAuth2AuthorizationServerConfiguration.class, OAuth2MethodSecurityConfiguration.class})
@EnableTransactionManagement
@EnableConfigurationProperties({FafApiProperties.class})
public class FafApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(FafApiApplication.class, args);
  }
}
