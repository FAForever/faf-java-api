package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.AchievementDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Slf4j
@Component
public class AchievementLocalizationListener {

  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    AchievementLocalizationListener.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void translate(AchievementDefinition achievementDefinition) {
    SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

    achievementDefinition.setName(messageSourceAccessor.getMessage(achievementDefinition.getNameKey()));
    achievementDefinition.setDescription(messageSourceAccessor.getMessage(achievementDefinition.getDescriptionKey()));
  }
}
