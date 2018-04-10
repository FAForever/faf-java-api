package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.VotingChoice;
import com.google.common.base.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Component
public class VotingChoiceEnricher {
  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    VotingChoiceEnricher.messageSourceAccessor = messageSourceAccessor;
  }


  @PostLoad
  public void enhance(VotingChoice votingChoice) {
    votingChoice.setNumberOfAnswers(votingChoice.getVotingAnswers().size());
    votingChoice.setChoiceText(messageSourceAccessor.getMessage(votingChoice.getChoiceTextKey()));
    if (!Strings.isNullOrEmpty(votingChoice.getDescriptionKey())) {
      votingChoice.setDescription(messageSourceAccessor.getMessage(votingChoice.getDescriptionKey()));
    }
  }


}
