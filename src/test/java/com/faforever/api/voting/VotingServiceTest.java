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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    votingSubject.setVotingQuestions(Set.of(votingQuestion));

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

    ApiException result = assertThrows(ApiException.class, () -> instance.saveVote(vote, new Player()));
    assertThat(result, hasErrorCode(ErrorCode.VOTING_SUBJECT_DOES_NOT_EXIST));

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
    votingSubject.setVotingQuestions(Set.of(votingQuestion));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.of(new Vote()));
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));

    ApiException result = assertThrows(ApiException.class, () -> instance.saveVote(vote, player));
    assertThat(result, hasErrorCode(ErrorCode.VOTED_TWICE));

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
    votingSubject.setVotingQuestions(Set.of(votingQuestion));
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

    vote.setVotingAnswers(Set.of(votingAnswer, votingAnswer2));

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
    votingSubject.setVotingQuestions(Set.of(votingQuestion));
    votingQuestion.setMaxAnswers(2);

    Vote vote = new Vote();

    VotingAnswer votingAnswer = new VotingAnswer();
    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);
    votingAnswer.setVotingChoice(votingChoice);
    votingAnswer.setAlternativeOrdinal(0);

    vote.setVotingAnswers(Set.of(votingAnswer));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));

    ApiException result = assertThrows(ApiException.class, () -> instance.saveVote(vote, player));
    assertThat(result, hasErrorCode(ErrorCode.VOTING_CHOICE_DOES_NOT_EXIST));

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
    votingSubject.setVotingQuestions(Set.of(votingQuestion));
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

    vote.setVotingAnswers(Set.of(votingAnswer, votingAnswer2));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));
    when(votingChoiceRepository.findById(anyInt())).thenReturn(Optional.of(votingChoice)).thenReturn(Optional.of(votingChoice2));

    ApiException result = assertThrows(ApiException.class, () -> instance.saveVote(vote, player));
    assertThat(result, hasErrorCode(ErrorCode.MALFORMATTED_ALTERNATIVE_ORDINALS));

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
    votingSubject.setVotingQuestions(Set.of(votingQuestion));
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

    vote.setVotingAnswers(Set.of(votingAnswer, votingAnswer2));

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));
    when(votingChoiceRepository.findById(anyInt())).thenReturn(Optional.of(votingChoice)).thenReturn(Optional.of(votingChoice2));

    ApiException result = assertThrows(ApiException.class, () -> instance.saveVote(vote, player));
    assertThat(result, hasErrorCode(ErrorCode.TOO_MANY_ANSWERS));

    verify(voteRepository, never()).save(vote);
  }

  @Test
  public void notSaveVoteOnNotEnoughGames() {
    Vote vote = new Vote();
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(false);
    votingQuestion.setMaxAnswers(1);
    votingSubject.setVotingQuestions(Set.of(votingQuestion));
    votingSubject.setMinGamesToVote(100);

    vote.setVotingSubject(votingSubject);
    Player player = new Player();

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));

    ApiException result = assertThrows(ApiException.class, () -> instance.saveVote(vote, player));
    assertThat(result, hasErrorCode(ErrorCode.NOT_ENOUGH_GAMES));

    verify(voteRepository, never()).save(vote);
  }

  @Test
  public void saveVoteOnNotEnoughGamesButSteamLinkAndAccountAge() {
    Vote vote = new Vote();
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setBeginOfVoteTime(OffsetDateTime.now());
    votingSubject.setEndOfVoteTime(OffsetDateTime.MAX);
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(false);
    votingQuestion.setMaxAnswers(1);
    votingSubject.setVotingQuestions(Set.of(votingQuestion));
    votingSubject.setMinGamesToVote(100);

    vote.setVotingSubject(votingSubject);
    Player player = new Player();
    player.setSteamId("someSteamId");
    player.setCreateTime(OffsetDateTime.now().minus(5, ChronoUnit.YEARS));

    when(voteRepository.findByPlayerAndVotingSubjectId(player, votingSubject.getId())).thenReturn(Optional.empty());
    when(votingSubjectRepository.findById(votingSubject.getId())).thenReturn(Optional.of(votingSubject));

    instance.saveVote(vote, player);
    verify(voteRepository).save(vote);
  }
}
