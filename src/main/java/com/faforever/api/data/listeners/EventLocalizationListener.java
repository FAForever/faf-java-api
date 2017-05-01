package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Slf4j
@Component
public class EventLocalizationListener {

  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    EventLocalizationListener.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void translate(Event event) {
    event.setName(messageSourceAccessor.getMessage(event.getNameKey()));
  }
}
