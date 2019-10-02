package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Achievement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.persistence.PostLoad;

@Slf4j
@Component
public class AchievementLocalizationListener {

  private static MessageSourceAccessor messageSourceAccessor;

  public void init(MessageSourceAccessor messageSourceAccessor) {
    AchievementLocalizationListener.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void translate(Achievement achievement) {
    achievement.setName(messageSourceAccessor.getMessage(achievement.getNameKey()));
    achievement.setDescription(messageSourceAccessor.getMessage(achievement.getDescriptionKey()));
  }
}
