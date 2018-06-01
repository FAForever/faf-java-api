package com.faforever.api.voting;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Validity;
import com.faforever.api.data.domain.Vote;
import com.faforever.api.data.domain.VotingAnswer;
import com.faforever.api.data.domain.VotingChoice;
import com.faforever.api.data.domain.VotingSubject;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.game.GamePlayerStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VotingService {
  private final VoteRepository voteRepository;
  private final VotingSubjectRepository votingSubjectRepository;
  private final GamePlayerStatsRepository gamePlayerStatsRepository;
  private final VotingChoiceRepository votingChoiceRepository;

  public VotingService(VoteRepository voteRepository, VotingSubjectRepository votingSubjectRepository, GamePlayerStatsRepository gamePlayerStatsRepository, VotingChoiceRepository votingChoiceRepository) {
    this.voteRepository = voteRepository;
    this.votingSubjectRepository = votingSubjectRepository;
    this.gamePlayerStatsRepository = gamePlayerStatsRepository;
    this.votingChoiceRepository = votingChoiceRepository;
  }

  @Transactional
  public void saveVote(Vote vote, Player player) {
    vote.setPlayer(player);
    Assert.notNull(vote.getVotingSubject(), "You must specify a subject");
    List<Error> errors = ableToVote(player, vote.getVotingSubject().getId());

    if (vote.getVotingAnswers() == null) {
      vote.setVotingAnswers(Collections.emptySet());
    }

    VotingSubject subject = votingSubjectRepository.findById(vote.getVotingSubject().getId())
      .orElseThrow(() -> new IllegalArgumentException("Subject of vote not found"));

    vote.getVotingAnswers().forEach(votingAnswer -> {
      VotingChoice votingChoice = votingAnswer.getVotingChoice();
      VotingChoice one = votingChoiceRepository.findById(votingChoice.getId())
        .orElseThrow(() -> new ApiException(new Error(ErrorCode.VOTING_CHOICE_DOES_NOT_EXIST, votingChoice.getId())));
      votingAnswer.setVotingChoice(one);
      votingAnswer.setVote(vote);
    });

    vote.getVotingAnswers().forEach(votingAnswer -> {
      long count = vote.getVotingAnswers().stream()
        .filter(votingAnswer1 -> votingAnswer1.getVotingChoice().equals(votingAnswer.getVotingChoice()))
        .count();
      if (count > 1) {
        errors.add(new Error(ErrorCode.VOTED_TWICE_ON_ONE_OPTION, votingAnswer.getVotingChoice().getId()));
      }
    });

    subject.getVotingQuestions().forEach(votingQuestion -> {
      List<VotingAnswer> votingAnswers = vote.getVotingAnswers().stream()
        .filter(votingAnswer -> votingAnswer.getVotingChoice().getVotingQuestion().equals(votingQuestion))
        .collect(Collectors.toList());
      long countOfAnswers = votingAnswers.size();
      int maxAnswers = votingQuestion.getMaxAnswers();
      if (maxAnswers < countOfAnswers) {
        errors.add(new Error(ErrorCode.TOO_MANY_ANSWERS, countOfAnswers, maxAnswers));
      }

      if (votingQuestion.isAlternativeQuestion()) {
        for (int i = 0; i < countOfAnswers; i++) {
          int finalI = i;
          long answersWithOrdinal = votingAnswers.stream()
            .filter(votingAnswer -> Objects.equals(votingAnswer.getAlternativeOrdinal(), finalI))
            .count();
          if (answersWithOrdinal == 1) {
            continue;
          }
          errors.add(new Error(ErrorCode.MALFORMATTED_ALTERNATIVE_ORDINALS));
        }
      }
    });
    if (!errors.isEmpty()) {
      throw new ApiException(errors.toArray(new Error[0]));
    }
    voteRepository.save(vote);
  }

  private List<Error> ableToVote(Player player, int votingSubjectId) {
    VotingSubject subject = votingSubjectRepository.findById(votingSubjectId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.VOTING_SUBJECT_DOES_NOT_EXIST, votingSubjectId)));

    List<Error> errors = new ArrayList<>();
    Optional<Vote> byPlayerAndVotingSubject = voteRepository.findByPlayerAndVotingSubjectId(player, votingSubjectId);
    if (byPlayerAndVotingSubject.isPresent()) {
      errors.add(new Error(ErrorCode.VOTED_TWICE));
    }

    int gamesPlayed = gamePlayerStatsRepository.countByPlayerAndGameValidity(player, Validity.VALID);

    if (subject.getBeginOfVoteTime().isAfter(OffsetDateTime.now())) {
      errors.add(new Error(ErrorCode.VOTE_DID_NOT_START_YET, subject.getBeginOfVoteTime()));
    }

    if (subject.getEndOfVoteTime().isBefore(OffsetDateTime.now())) {
      errors.add(new Error(ErrorCode.VOTE_ALREADY_ENDED, subject.getEndOfVoteTime()));
    }

    if (gamesPlayed < subject.getMinGamesToVote()) {
      errors.add(new Error(ErrorCode.NOT_ENOUGH_GAMES, gamesPlayed, subject.getMinGamesToVote()));
    }
    return errors;
  }

  List<VotingSubject> votingSubjectsAbleToVote(Player player) {
    List<VotingSubject> all = votingSubjectRepository.findAll();
    return all.stream()
      .filter(votingSubject -> ableToVote(player, votingSubject.getId()).isEmpty())
      .collect(Collectors.toList());
  }
}
