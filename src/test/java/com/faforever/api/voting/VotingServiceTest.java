package com.faforever.api.voting;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Vote;
import com.faforever.api.data.domain.VotingAnswer;
import com.faforever.api.data.domain.VotingChoice;
import com.faforever.api.data.domain.VotingQuestion;
import com.faforever.api.data.domain.VotingSubject;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.game.GamePlayerStatsRepository;
import com.faforever.api.player.PlayerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VotingServiceTest {
  private VotingService instance;
  @Mock
  private VoteRepository voteRepository;
  @Mock
  private VotingSubjectRepository votingSubjectRepository;
  @Mock
  private GamePlayerStatsRepository gamePlayerStatsRepository;
  @Mock
  private VotingChoiceRepository votingChoiceRepository;
  @Mock
  private PlayerRepository playerRepository;

  @Before
  public void setUp() {
    instance = new VotingService(voteRepository, votingSubjectRepository, gamePlayerStatsRepository, votingChoiceRepository, playerRepository);
  }

  @Test
  public void saveVoteSuccessful() {
    Vote vote = new Vote();
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(false);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findOne(votingSubject.getId())).thenReturn(votingSubject);

    instance.saveVote(vote, player);
    verify(voteRepository).save(vote);
  }


  @Test
  public void notSaveVoteIfUserVotedAlready() {
    Vote vote = new Vote();
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(false);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.of(new Vote()));
    when(votingSubjectRepository.findOne(votingSubject.getId())).thenReturn(votingSubject);

    try {
      instance.saveVote(vote, player);
    } catch (ApiException e) {
      assertTrue(Arrays.stream(e.getErrors()).anyMatch(error -> error.getErrorCode().equals(ErrorCode.VOTED_TWICE)));
    }
    verify(voteRepository, never()).save(vote);
  }

  @Test
  public void saveVoteIfAlternativeOrdinalCorrect() {
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);

    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(true);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));
    votingQuestion.setMaxAnswers(2);

    Vote vote = new Vote();

    VotingAnswer votingAnswer = new VotingAnswer();
    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);
    votingAnswer.setVotingChoice(votingChoice);
    votingAnswer.setAlternativeOrdinal(0);
    VotingAnswer votingAnswer2 = new VotingAnswer();
    VotingChoice votingChoice2 = new VotingChoice();
    votingChoice2.setId(2);
    votingChoice2.setVotingQuestion(votingQuestion);
    votingAnswer2.setVotingChoice(votingChoice2);
    votingAnswer2.setAlternativeOrdinal(1);

    vote.setVotingAnswers(new HashSet<>(Arrays.asList(votingAnswer, votingAnswer2)));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findOne(votingSubject.getId())).thenReturn(votingSubject);
    when(votingChoiceRepository.findOne(votingChoice.getId())).thenReturn(votingChoice);
    when(votingChoiceRepository.findOne(votingChoice2.getId())).thenReturn(votingChoice2);

    instance.saveVote(vote, player);
    verify(voteRepository).save(vote);
  }

  @Test
  public void notSaveVoteIfAlternativeOrdinalWrong() {
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);

    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(true);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));
    votingQuestion.setMaxAnswers(2);

    Vote vote = new Vote();

    VotingAnswer votingAnswer = new VotingAnswer();
    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);
    votingAnswer.setVotingChoice(votingChoice);
    votingAnswer.setAlternativeOrdinal(1);
    VotingAnswer votingAnswer2 = new VotingAnswer();
    VotingChoice votingChoice2 = new VotingChoice();
    votingChoice2.setId(2);
    votingChoice2.setVotingQuestion(votingQuestion);
    votingAnswer2.setVotingChoice(votingChoice2);
    votingAnswer2.setAlternativeOrdinal(1);

    vote.setVotingAnswers(new HashSet<>(Arrays.asList(votingAnswer, votingAnswer2)));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findOne(votingSubject.getId())).thenReturn(votingSubject);
    when(votingChoiceRepository.findOne(votingChoice.getId())).thenReturn(votingChoice);
    when(votingChoiceRepository.findOne(votingChoice2.getId())).thenReturn(votingChoice2);

    try {
      instance.saveVote(vote, player);
    } catch (ApiException e) {
      assertTrue(Arrays.stream(e.getErrors()).anyMatch(error -> error.getErrorCode().equals(ErrorCode.MALFORMATTED_ALTERNATIVE_ORDINALS)));
    }
    verify(voteRepository, never()).save(vote);
  }

  @Test
  public void notSaveVoteOnTooManyAnswers() {
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);

    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(true);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));
    votingQuestion.setMaxAnswers(1);

    Vote vote = new Vote();

    VotingAnswer votingAnswer = new VotingAnswer();
    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);
    votingAnswer.setVotingChoice(votingChoice);
    VotingAnswer votingAnswer2 = new VotingAnswer();
    VotingChoice votingChoice2 = new VotingChoice();
    votingChoice2.setId(2);
    votingChoice2.setVotingQuestion(votingQuestion);
    votingAnswer2.setVotingChoice(votingChoice2);

    vote.setVotingAnswers(new HashSet<>(Arrays.asList(votingAnswer, votingAnswer2)));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findOne(votingSubject.getId())).thenReturn(votingSubject);
    when(votingChoiceRepository.findOne(votingChoice.getId())).thenReturn(votingChoice);
    when(votingChoiceRepository.findOne(votingChoice2.getId())).thenReturn(votingChoice2);

    try {
      instance.saveVote(vote, player);
    } catch (ApiException e) {
      assertTrue(Arrays.stream(e.getErrors()).anyMatch(error -> error.getErrorCode().equals(ErrorCode.TOO_MANY_ANSWERS)));
    }
    verify(voteRepository, never()).save(vote);
  }

  @Test
  public void notSaveOnVoteTwiceInOneOption() {
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);

    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(true);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));
    votingQuestion.setMaxAnswers(2);

    Vote vote = new Vote();

    VotingAnswer votingAnswer = new VotingAnswer();
    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);
    votingAnswer.setVotingChoice(votingChoice);
    VotingAnswer votingAnswer2 = new VotingAnswer();
    votingAnswer2.setVotingChoice(votingChoice);

    vote.setVotingAnswers(new HashSet<>(Arrays.asList(votingAnswer, votingAnswer2)));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findOne(votingSubject.getId())).thenReturn(votingSubject);
    when(votingChoiceRepository.findOne(votingChoice.getId())).thenReturn(votingChoice);
    try {
      instance.saveVote(vote, player);
    } catch (ApiException e) {
      assertTrue(Arrays.stream(e.getErrors()).anyMatch(error -> error.getErrorCode().equals(ErrorCode.VOTED_TWICE_ON_ONE_OPTION)));
    }
    verify(voteRepository, never()).save(vote);
  }
}
