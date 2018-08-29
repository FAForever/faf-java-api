package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.VotingAnswer;
import com.faforever.api.data.domain.VotingChoice;
import com.faforever.api.data.domain.VotingQuestion;
import com.faforever.api.data.domain.VotingSubject;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;
import javax.persistence.PreUpdate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class VotingSubjectEnricher {
  private static MessageSourceAccessor messageSourceAccessor;


  @Inject
  public void init(MessageSourceAccessor messageSourceAccessor) {
    VotingSubjectEnricher.messageSourceAccessor = messageSourceAccessor;
  }

  @PreUpdate
  public void preUpdate(VotingSubject votingSubject) {
    updateWinners(votingSubject);
  }

  private void updateWinners(VotingSubject votingSubject) {
    votingSubject.getVotingQuestions().forEach(this::calculateWinners);
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

  @VisibleForTesting
  void calculateWinners(VotingQuestion votingQuestion) {
    VotingSubject votingSubject = votingQuestion.getVotingSubject();
    boolean ended = votingSubject.getEndOfVoteTime().isBefore(OffsetDateTime.now());
    List<VotingChoice> winners = votingQuestion.getWinners();
    if ((winners == null || winners.isEmpty()) && ended && votingSubject.getRevealWinner()) {
      votingQuestion.setWinners(getWinners(votingQuestion));
    } else {
      votingQuestion.setWinners(Collections.emptyList());
    }
  }

  private List<VotingChoice> getWinners(VotingQuestion votingQuestion) {
    if (!votingQuestion.isAlternativeQuestion()) {
      OptionalInt max = votingQuestion.getVotingChoices().stream()
        .mapToInt(value -> value.getVotingAnswers().size()).max();
      if (max.isPresent()) {
        return votingQuestion.getVotingChoices().stream()
          .filter(votingChoice -> votingChoice.getVotingAnswers().size() == max.getAsInt()).collect(toList());
      }
      return Collections.emptyList();
    }

    //All the answers sorted by their choice, but only those that are the 1st choice
    Map<VotingChoice, List<VotingAnswer>> votersByChoice = votingQuestion.getVotingChoices().stream()
      .collect(Collectors.toMap(
        Function.identity(),
        choice -> new ArrayList<>(choice.getVotingAnswers().stream()
          .filter(votingAnswer -> votingAnswer.getAlternativeOrdinal() == 0)
          .collect(toList()))
      ));

    while (votersByChoice.size() > 1) {
      OptionalInt min = votersByChoice.values().stream().mapToInt(List::size).min();
      List<VotingChoice> candidatesToEliminate = votersByChoice.entrySet().stream()
        .filter(votingChoiceListEntry -> votingChoiceListEntry.getValue().size() == min.getAsInt())
        .map(Entry::getKey)
        .collect(toList());

      if (candidatesToEliminate.size() == votersByChoice.size()) {
        //We got a problem here, we would eliminate all the candidates if we went on normally
        return candidatesToEliminate;
      }

      candidatesToEliminate.forEach(candidate -> {
        List<VotingAnswer> votingAnswersForCandidate = votersByChoice.get(candidate);

        //Lets distribute the answers of the candidate that is eliminated
        votingAnswersForCandidate.forEach(votingAnswer -> {
          int newAlternativeOrdinal = votingAnswer.getAlternativeOrdinal() + 1;
          votingAnswer.getVote().getVotingAnswers().stream()
            .filter(votingAnswer1 -> votingAnswer1.getVotingChoice().getVotingQuestion().equals(votingAnswer.getVotingChoice().getVotingQuestion()) && votingAnswer1.getAlternativeOrdinal() == newAlternativeOrdinal)
            .findFirst()
            .ifPresent(votingAnswer1 -> {
              VotingChoice votingChoice1 = votingAnswer1.getVotingChoice();
              votersByChoice.get(votingChoice1).add(votingAnswer1);
            });
        });

        votersByChoice.remove(candidate);
      });
    }
    Optional<Entry<VotingChoice, List<VotingAnswer>>> first = votersByChoice.entrySet().stream().findFirst();

    return first.map(votingChoiceListEntry -> Collections.singletonList(votingChoiceListEntry.getKey())).orElse(Collections.emptyList());
  }
}
