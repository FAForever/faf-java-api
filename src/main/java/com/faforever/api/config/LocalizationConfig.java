package com.faforever.api.config;

import com.faforever.api.i18n.RepositoryMessageSource;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;

import jakarta.inject.Inject;

@Configuration
public class LocalizationConfig {

  @Bean
  public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
    return new MessageSourceAccessor(messageSource);
  }

  @Inject
  public void configureMessageSource(HierarchicalMessageSource messageSource, RepositoryMessageSource repositoryMessageSource) {
    messageSource.setParentMessageSource(repositoryMessageSource);
  }
}
