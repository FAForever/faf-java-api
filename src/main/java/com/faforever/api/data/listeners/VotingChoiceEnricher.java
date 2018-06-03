package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.VotingAnswer;
import com.faforever.api.data.domain.VotingChoice;
import com.google.common.base.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;
import java.util.Set;

@Component
public class VotingChoiceEnricher {
  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    VotingChoiceEnricher.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void enhance(VotingChoice votingChoice) {
    Boolean revealWinner = votingChoice.getVotingQuestion().getVotingSubject().getRevealWinner();
    Set<VotingAnswer> votingAnswers = votingChoice.getVotingAnswers();
    votingChoice.setNumberOfAnswers(revealWinner && votingAnswers != null ? votingAnswers.size() : 0);
    votingChoice.setChoiceText(messageSourceAccessor.getMessage(votingChoice.getChoiceTextKey()));
    if (!Strings.isNullOrEmpty(votingChoice.getDescriptionKey())) {
      votingChoice.setDescription(messageSourceAccessor.getMessage(votingChoice.getDescriptionKey()));
    }
  }


}
