package com.faforever.api.voting;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Vote;
import com.faforever.api.data.domain.VotingAnswer;
import com.faforever.api.data.domain.VotingChoice;
import com.faforever.api.data.domain.VotingQuestion;
import com.faforever.api.data.domain.VotingSubject;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ApiExceptionMatcher;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.game.GamePlayerStatsRepository;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExpectedExceptionSupport;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ExpectedExceptionSupport.class})
public class VotingServiceTest {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private VotingService instance;
  @Mock
  private VoteRepository voteRepository;
  @Mock
  private VotingSubjectRepository votingSubjectRepository;
  @Mock
  private GamePlayerStatsRepository gamePlayerStatsRepository;
  @Mock
  private VotingChoiceRepository votingChoiceRepository;

  @BeforeEach
  public void setUp() {
    instance = new VotingService(voteRepository, votingSubjectRepository, gamePlayerStatsRepository, votingChoiceRepository);
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
    votingQuestion.setMaxAnswers(1);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));

    instance.saveVote(vote, player);
    verify(voteRepository).save(vote);
  }

  @Test
  public void saveVoteInvalidVoteId() {
    Vote vote = new Vote();
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);

    vote.setVotingSubject(votingSubject);

    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.VOTING_SUBJECT_DOES_NOT_EXIST));

    instance.saveVote(vote, new Player());
    verify(voteRepository, never()).save(vote);
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
    votingQuestion.setMaxAnswers(1);
    votingSubject.setVotingQuestions(Collections.singleton(votingQuestion));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.of(new Vote()));
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));

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
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));
    when(votingChoiceRepository.findById(anyInt())).thenReturn(Optional.of(votingChoice)).thenReturn(Optional.of(votingChoice2));

    instance.saveVote(vote, player);
    verify(voteRepository).save(vote);
  }

  @Test
  public void saveVoteInvalidChoiceId() {
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

    vote.setVotingAnswers(new HashSet<>(Collections.singletonList(votingAnswer)));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));

    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.VOTING_CHOICE_DOES_NOT_EXIST));

    instance.saveVote(vote, player);
    verify(voteRepository, never()).save(vote);
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
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));
    when(votingChoiceRepository.findById(anyInt())).thenReturn(Optional.of(votingChoice)).thenReturn(Optional.of(votingChoice2));

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
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));
    when(votingChoiceRepository.findById(anyInt())).thenReturn(Optional.of(votingChoice)).thenReturn(Optional.of(votingChoice2));

    try {
      instance.saveVote(vote, player);
    } catch (ApiException e) {
      assertTrue(Arrays.stream(e.getErrors()).anyMatch(error -> error.getErrorCode().equals(ErrorCode.TOO_MANY_ANSWERS)));
    }
    verify(voteRepository, never()).save(vote);
  }
}
