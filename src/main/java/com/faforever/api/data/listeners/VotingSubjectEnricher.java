package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.VotingSubject;
import com.google.common.base.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Component
public class VotingSubjectEnricher {
  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    VotingSubjectEnricher.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void enhance(VotingSubject votingSubject) {
    votingSubject.setNumberOfVotes(votingSubject.getVotes().size());
    votingSubject.setSubject(messageSourceAccessor.getMessage(votingSubject.getSubjectKey()));
    String descriptionKey = votingSubject.getDescriptionKey();
    if (!Strings.isNullOrEmpty(descriptionKey)) {
      votingSubject.setDescription(messageSourceAccessor.getMessage(votingSubject.getDescriptionKey()));
    }
  }
}
