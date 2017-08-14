package com.faforever.api.config;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(ApplicationProfile.PRODUCTION)
public class MandrillConfig {

  @Bean
  public MandrillApi mandrillApi(FafApiProperties properties) {
    return new MandrillApi(properties.getMail().getMandrillApiKey());
  }
}
