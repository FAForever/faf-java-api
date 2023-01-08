package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Tutorial;
import com.google.common.base.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

@Component
public class TutorialEnricherListener {
  private static FafApiProperties fafApiProperties;
  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(FafApiProperties fafApiProperties, MessageSourceAccessor messageSourceAccessor) {
    TutorialEnricherListener.fafApiProperties = fafApiProperties;
    TutorialEnricherListener.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void enrich(Tutorial tutorial) {
    if (!Strings.isNullOrEmpty(tutorial.getImage())) {
      tutorial.setImageUrl(String.format(fafApiProperties.getTutorial().getThumbnailUrlFormat(), tutorial.getImage()));
    }
    tutorial.setTitle(messageSourceAccessor.getMessage(tutorial.getTitleKey()));
    if (!Strings.isNullOrEmpty(tutorial.getDescriptionKey())) {
      tutorial.setDescription(messageSourceAccessor.getMessage(tutorial.getDescriptionKey()));
    }
  }
}
