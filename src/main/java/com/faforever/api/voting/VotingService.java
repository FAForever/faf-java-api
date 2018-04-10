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
import com.faforever.api.player.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VotingService {
  private final VoteRepository voteRepository;
  private final VotingAnswerRepository votingAnswerRepository;
  private final VotingSubjectRepository votingSubjectRepository;
  private final GamePlayerStatsRepository gamePlayerStatsRepository;
  private final VotingChoiceRepository votingChoiceRepository;
  private final PlayerRepository playerRepository;

  public VotingService(VoteRepository voteRepository, VotingAnswerRepository votingAnswerRepository, VotingSubjectRepository votingSubjectRepository, GamePlayerStatsRepository gamePlayerStatsRepository, VotingChoiceRepository votingChoiceRepository, PlayerRepository playerRepository) {
    this.voteRepository = voteRepository;
    this.votingAnswerRepository = votingAnswerRepository;
    this.votingSubjectRepository = votingSubjectRepository;
    this.gamePlayerStatsRepository = gamePlayerStatsRepository;
    this.votingChoiceRepository = votingChoiceRepository;
    this.playerRepository = playerRepository;
  }

  @Transactional
  public void saveVote(Vote vote, Player player) {
    vote.setPlayer(player);
    Assert.notNull(vote.getVotingSubject(), "You must specify a subject");
    ApiException apiException = ableToVote(player, vote.getVotingSubject().getId());
    if (apiException != null) {
      throw apiException;
    }

    if (vote.getVotingAnswers() == null) {
      vote.setVotingAnswers(Collections.emptySet());
    }

    VotingSubject subject = votingSubjectRepository.findOne(vote.getVotingSubject().getId());

    vote.getVotingAnswers().forEach(votingAnswer -> {
      VotingChoice votingChoice = votingAnswer.getVotingChoice();
      VotingChoice one = votingChoiceRepository.findOne(votingChoice.getId());
      votingAnswer.setVotingChoice(one);
      votingAnswer.setVote(vote);
    });

    subject.getVotingQuestions().forEach(votingQuestion -> {
      List<VotingAnswer> votingAnswers = vote.getVotingAnswers().stream()
        .filter(votingAnswer -> votingAnswer.getVotingChoice().getVotingQuestion().equals(votingQuestion)).collect(Collectors.toList());
      long countOfAnswers = votingAnswers.size();
      int maxAnswers = votingQuestion.getMaxAnswers();
      if (maxAnswers < countOfAnswers) {
        throw new ApiException(new Error(ErrorCode.TOO_MANY_ANSWERS, countOfAnswers, maxAnswers));
      }

      if (votingQuestion.isAlternativeQuestion()) {
        for (int i = 0; i < countOfAnswers; i++) {
          int finalI = i;
          long answersWithOrdinal = votingAnswers.stream().filter(votingAnswer -> votingAnswer.getAlternativeOrdinal() == finalI).count();
          if (answersWithOrdinal == 0) {
            break;
          } else if (answersWithOrdinal == 1) {
            continue;
          }
          throw new ApiException(new Error(ErrorCode.MAL_FORMATTED_ALTERNATIVE_ORDINALS));
        }
      }
    });

    voteRepository.save(vote);
  }

  ApiException ableToVote(int playerId, int votingSubjectId) {
    Player player = playerRepository.findOne(playerId);
    return ableToVote(player, votingSubjectId);
  }

  ApiException ableToVote(Player player, int votingSubjectId) {
    Optional<Vote> byPlayerAndVotingSubject = voteRepository.findByPlayerAndVotingSubjectId(player, votingSubjectId);
    if (byPlayerAndVotingSubject.isPresent()) {
      return new ApiException(new Error(ErrorCode.VOTED_TWICE));
    }
    VotingSubject subject = votingSubjectRepository.findOne(votingSubjectId);
    int gamesPlayed = gamePlayerStatsRepository.countByPlayerAndGameValidity(player, Validity.VALID);

    if (subject.getBeginOfVoteTime().isAfter(OffsetDateTime.now())) {
      return new ApiException(new Error(ErrorCode.VOTE_DID_NOT_START_YET, subject.getBeginOfVoteTime()));
    }

    if (subject.getEndOfVoteTime().isBefore(OffsetDateTime.now())) {
      return new ApiException(new Error(ErrorCode.VOTE_ALREADY_ENDED, subject.getEndOfVoteTime()));
    }

    if (gamesPlayed < subject.getMinGamesToVote()) {
      return new ApiException(new Error(ErrorCode.NOT_ENOUGH_GAMES, gamesPlayed, subject.getMinGamesToVote()));
    }
    return null;
  }

  List<VotingSubject> votingSubjectsAbleToVote(int userId) {
    return votingSubjectsAbleToVote(playerRepository.findOne(userId));
  }

  List<VotingSubject> votingSubjectsAbleToVote(Player player) {
    List<VotingSubject> all = votingSubjectRepository.findAll();
    return all.stream()
      .filter(votingSubject -> ableToVote(player, votingSubject.getId()) == null)
      .collect(Collectors.toList());
  }
}
