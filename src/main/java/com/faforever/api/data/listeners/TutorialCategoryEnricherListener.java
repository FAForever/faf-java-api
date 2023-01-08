package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.TutorialCategory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

@Component
public class TutorialCategoryEnricherListener {
  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    TutorialCategoryEnricherListener.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void enrich(TutorialCategory tutorialCategory) {
    tutorialCategory.setCategory(messageSourceAccessor.getMessage(tutorialCategory.getCategoryKey()));
  }
}
