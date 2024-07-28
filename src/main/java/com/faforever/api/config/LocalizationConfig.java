package com.faforever.api.config;

import com.faforever.api.i18n.RepositoryMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;

@Configuration
public class LocalizationConfig {

  @Bean
  public MessageSourceAccessor messageSourceAccessor(RepositoryMessageSource messageSource) {
    return new MessageSourceAccessor(messageSource);
  }
}
