package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.VotingQuestion;
import com.google.common.base.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

@Component
public class VotingQuestionEnricher {
  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    VotingQuestionEnricher.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void enhance(VotingQuestion votingQuestion) {
    if (votingQuestion.getVotingChoices() != null) {
      int numberOfAnswers = votingQuestion.getVotingChoices().stream()
        .mapToInt(votingChoice -> {
          if (votingChoice.getVotingAnswers() == null) {
            return 0;
          }
          return votingChoice.getVotingAnswers().size();
        })
        .sum();
      votingQuestion.setNumberOfAnswers(numberOfAnswers);
    }
    votingQuestion.setQuestion(messageSourceAccessor.getMessage(votingQuestion.getQuestionKey()));
    if (!Strings.isNullOrEmpty(votingQuestion.getDescriptionKey())) {
      votingQuestion.setDescription(messageSourceAccessor.getMessage(votingQuestion.getDescriptionKey()));
    }
  }

}
