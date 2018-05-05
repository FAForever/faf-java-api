package com.faforever.api.config;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "faf-api.mail.mandrill-api-key")
public class MandrillConfig {

  @Bean
  public MandrillApi mandrillApi(FafApiProperties properties) {
    return new MandrillApi(properties.getMail().getMandrillApiKey());
  }
}
