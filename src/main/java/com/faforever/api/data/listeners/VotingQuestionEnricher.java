package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.VotingAnswer;
import com.faforever.api.data.domain.VotingChoice;
import com.faforever.api.data.domain.VotingQuestion;
import com.google.common.base.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class VotingQuestionEnricher {
  private static MessageSourceAccessor messageSourceAccessor;

  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    VotingQuestionEnricher.messageSourceAccessor = messageSourceAccessor;
  }

  @PostLoad
  public void enhance(VotingQuestion votingQuestion) {
    long numberOfAnswers = votingQuestion.getVotingChoices().stream()
      .mapToLong(votingChoice -> votingChoice.getVotingAnswers().size())
      .sum();
    votingQuestion.setNumberOfAnswers((int) numberOfAnswers);
    votingQuestion.setQuestion(messageSourceAccessor.getMessage(votingQuestion.getQuestionKey()));
    if (!Strings.isNullOrEmpty(votingQuestion.getDescriptionKey())) {
      votingQuestion.setDescription(messageSourceAccessor.getMessage(votingQuestion.getDescriptionKey()));
    }

    votingQuestion.setAlternativeWinner(getAlternativeWinner(votingQuestion));
  }

  private VotingChoice getAlternativeWinner(VotingQuestion votingQuestion) {
    if (!votingQuestion.isAlternativeQuestion()) {
      return null;
    }
    Map<VotingChoice, List<VotingAnswer>> votersByChoice = votingQuestion.getVotingChoices().stream()
      .collect(Collectors.toMap(
        o -> o,
        o -> new ArrayList<>(o.getVotingAnswers().stream()
          .filter(votingAnswer -> votingAnswer.getAlternativeOrdinal() == 0)
          .collect(toList()))
      ));

    while (votersByChoice.size() > 1) {
      Optional<Entry<VotingChoice, List<VotingAnswer>>> min = votersByChoice.entrySet().stream().min(Comparator.comparingInt(o -> o.getValue().size()));
      Entry<VotingChoice, List<VotingAnswer>> minCandidate = min.get();
      minCandidate.getValue().forEach(votingAnswer -> {
        int newAlternativeOrdinal = votingAnswer.getAlternativeOrdinal() + 1;
        votingAnswer.getVote().getVotingAnswers().stream()
          .filter(votingAnswer1 -> votingAnswer1.getVotingChoice().getVotingQuestion().equals(votingAnswer.getVotingChoice().getVotingQuestion()) && votingAnswer1.getAlternativeOrdinal() == newAlternativeOrdinal)
          .findFirst()
          .ifPresent(votingAnswer1 -> {
            VotingChoice votingChoice1 = votingAnswer1.getVotingChoice();
            votersByChoice.get(votingChoice1).add(votingAnswer1);
          });
      });
      votersByChoice.remove(minCandidate.getKey());
    }
    return votersByChoice.entrySet().stream().findFirst().get().getKey();
  }
}
