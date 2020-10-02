package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Vote;
import com.faforever.api.data.domain.VotingAnswer;
import com.faforever.api.data.domain.VotingChoice;
import com.faforever.api.data.domain.VotingQuestion;
import com.faforever.api.data.domain.VotingSubject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ExtendWith(MockitoExtension.class)
public class VotingSubjectEnricherTest {
  private VotingSubjectEnricher instance;
  @Mock
  private MessageSourceAccessor messageSourceAccessor;

  @BeforeEach
  public void setUp() {
    instance = new VotingSubjectEnricher();
    instance.init(messageSourceAccessor);
  }

  @Test
  public void testQuestionEnhancing() {
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setAlternativeQuestion(true);
    votingQuestion.setQuestionKey("abc");
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setEndOfVoteTime(OffsetDateTime.MIN);
    votingSubject.setRevealWinner(true);
    votingQuestion.setVotingSubject(votingSubject);

    Vote vote1 = new Vote();
    Player player1 = new Player();
    vote1.setPlayer(player1);

    Vote vote2 = new Vote();
    Player player2 = new Player();
    vote2.setPlayer(player2);

    Vote vote3 = new Vote();
    Player player3 = new Player();
    vote1.setPlayer(player3);

    Vote vote4 = new Vote();
    Player player4 = new Player();
    vote1.setPlayer(player4);

    Vote vote5 = new Vote();
    Player player5 = new Player();
    vote1.setPlayer(player5);

    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice, votingQuestion, vote1, 0);
    addAnswerToChoice(votingChoice, votingQuestion, vote2, 0);

    VotingChoice votingChoice2 = new VotingChoice();
    votingChoice2.setId(2);
    votingChoice2.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice2, votingQuestion, vote3, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote4, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote5, 1);

    VotingChoice votingChoice3 = new VotingChoice();
    votingChoice3.setId(3);
    votingChoice3.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice2, votingQuestion, vote5, 0);

    instance.calculateWinners(votingQuestion);

    assertThat(votingQuestion.getWinners(), hasItem(votingChoice2));
  }

  @Test
  public void testQuestionEnhancingDraw() {
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setId(1);
    votingQuestion.setAlternativeQuestion(true);
    votingQuestion.setQuestionKey("abc");
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setEndOfVoteTime(OffsetDateTime.MIN);
    votingSubject.setRevealWinner(true);
    votingQuestion.setVotingSubject(votingSubject);

    Vote vote1 = (Vote) new Vote().setId(1);
    Player player1 = (Player) new Player().setId(1);
    vote1.setPlayer(player1);

    Vote vote2 = (Vote) new Vote().setId(2);
    Player player2 = (Player) new Player().setId(2);
    vote2.setPlayer(player2);

    Vote vote3 = (Vote) new Vote().setId(3);
    Player player3 = (Player) new Player().setId(3);
    vote3.setPlayer(player3);

    Vote vote4 = (Vote) new Vote().setId(4);
    Player player4 = (Player) new Player().setId(4);
    vote4.setPlayer(player4);

    Vote vote5 = (Vote) new Vote().setId(5);
    Player player5 = (Player) new Player().setId(5);
    vote5.setPlayer(player5);

    Vote vote6 = (Vote) new Vote().setId(6);
    Player player6 = (Player) new Player().setId(6);
    vote6.setPlayer(player6);


    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice, votingQuestion, vote1, 0);
    addAnswerToChoice(votingChoice, votingQuestion, vote2, 0);
    addAnswerToChoice(votingChoice, votingQuestion, vote6, 0);


    VotingChoice votingChoice2 = new VotingChoice();
    votingChoice2.setId(2);
    votingChoice2.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice2, votingQuestion, vote4, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote3, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote5, 1);

    VotingChoice votingChoice3 = new VotingChoice();
    votingChoice3.setId(3);
    votingChoice3.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice3, votingQuestion, vote5, 0);

    instance.calculateWinners(votingQuestion);

    assertThat(votingQuestion.getWinners(), Matchers.allOf(hasItem(votingChoice2), hasItem(votingChoice)));
  }

  @Test
  public void testQuestionEnhancingDrawWithBlankOption() {
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setId(1);
    votingQuestion.setAlternativeQuestion(true);
    votingQuestion.setQuestionKey("abc");
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setEndOfVoteTime(OffsetDateTime.MIN);
    votingSubject.setRevealWinner(true);
    votingQuestion.setVotingSubject(votingSubject);

    Vote vote1 = (Vote) new Vote().setId(1);
    Player player1 = (Player) new Player().setId(1);
    vote1.setPlayer(player1);

    Vote vote2 = (Vote) new Vote().setId(2);
    Player player2 = (Player) new Player().setId(2);
    vote2.setPlayer(player2);

    Vote vote3 = (Vote) new Vote().setId(3);
    Player player3 = (Player) new Player().setId(3);
    vote3.setPlayer(player3);

    Vote vote4 = (Vote) new Vote().setId(4);
    Player player4 = (Player) new Player().setId(4);
    vote4.setPlayer(player4);

    Vote vote5 = (Vote) new Vote().setId(5);
    Player player5 = (Player) new Player().setId(5);
    vote5.setPlayer(player5);

    Vote vote6 = (Vote) new Vote().setId(6);
    Player player6 = (Player) new Player().setId(6);
    vote6.setPlayer(player6);

    Vote vote7 = (Vote) new Vote().setId(7);
    Player player7 = (Player) new Player().setId(7);
    vote6.setPlayer(player7);


    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice, votingQuestion, vote1, 0);
    addAnswerToChoice(votingChoice, votingQuestion, vote2, 0);
    addAnswerToChoice(votingChoice, votingQuestion, vote6, 0);


    VotingChoice votingChoice2 = new VotingChoice();
    votingChoice2.setId(2);
    votingChoice2.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice2, votingQuestion, vote4, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote3, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote5, 0);

    VotingChoice votingChoice3 = new VotingChoice();
    votingChoice3.setId(3);
    votingChoice3.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice3, votingQuestion, vote7, 0);

    addAnswerToChoice(null, votingQuestion, vote7, 1);

    instance.calculateWinners(votingQuestion);

    assertThat(votingQuestion.getWinners(), Matchers.allOf(hasItem(votingChoice2), hasItem(votingChoice)));
  }

  @Test
  public void testQuestionEnhancingDrawWithTwoCandidatesGettingEliminatedAtTheSameTime() {
    VotingQuestion votingQuestion = new VotingQuestion();
    votingQuestion.setId(1);
    votingQuestion.setAlternativeQuestion(true);
    votingQuestion.setQuestionKey("abc");
    VotingSubject votingSubject = new VotingSubject();
    votingSubject.setId(1);
    votingSubject.setEndOfVoteTime(OffsetDateTime.MIN);
    votingSubject.setRevealWinner(true);
    votingQuestion.setVotingSubject(votingSubject);

    Vote vote1 = (Vote) new Vote().setId(1);
    Player player1 = (Player) new Player().setId(1);
    vote1.setPlayer(player1);

    Vote vote2 = (Vote) new Vote().setId(2);
    Player player2 = (Player) new Player().setId(2);
    vote2.setPlayer(player2);

    Vote vote3 = (Vote) new Vote().setId(3);
    Player player3 = (Player) new Player().setId(3);
    vote3.setPlayer(player3);

    Vote vote4 = (Vote) new Vote().setId(4);
    Player player4 = (Player) new Player().setId(4);
    vote4.setPlayer(player4);

    Vote vote5 = (Vote) new Vote().setId(5);
    Player player5 = (Player) new Player().setId(5);
    vote5.setPlayer(player5);

    Vote vote6 = (Vote) new Vote().setId(6);
    Player player6 = (Player) new Player().setId(6);
    vote6.setPlayer(player6);

    Vote vote7 = (Vote) new Vote().setId(7);
    Player player7 = (Player) new Player().setId(7);
    vote6.setPlayer(player7);


    VotingChoice votingChoice = new VotingChoice();
    votingChoice.setId(1);
    votingChoice.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice, votingQuestion, vote1, 0);
    addAnswerToChoice(votingChoice, votingQuestion, vote2, 0);
    addAnswerToChoice(votingChoice, votingQuestion, vote3, 1);


    VotingChoice votingChoice2 = new VotingChoice();
    votingChoice2.setId(2);
    votingChoice2.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice2, votingQuestion, vote3, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote4, 0);
    addAnswerToChoice(votingChoice2, votingQuestion, vote1, 1);


    VotingChoice votingChoice3 = new VotingChoice();
    votingChoice3.setId(3);
    votingChoice3.setVotingQuestion(votingQuestion);

    addAnswerToChoice(votingChoice3, votingQuestion, vote5, 0);
    addAnswerToChoice(votingChoice3, votingQuestion, vote6, 0);
    addAnswerToChoice(votingChoice3, votingQuestion, vote7, 0);


    instance.calculateWinners(votingQuestion);

    assertThat(votingQuestion.getWinners(), is(Collections.singletonList(votingChoice3)));
  }

  private void addAnswerToChoice(VotingChoice votingChoice, VotingQuestion votingQuestion, Vote vote, int alternativeOrdinal) {
    VotingAnswer votingAnswer = new VotingAnswer();
    votingAnswer.setAlternativeOrdinal(alternativeOrdinal);
    votingAnswer.setVote(vote);
    votingAnswer.setVotingChoice(votingChoice);

    if (vote.getVotingAnswers() != null) {
      vote.getVotingAnswers().add(votingAnswer);
    } else {
      vote.setVotingAnswers(new HashSet<>(Collections.singleton(votingAnswer)));
    }

    if (votingChoice != null) {
      if (votingChoice.getVotingAnswers() != null) {
        votingChoice.getVotingAnswers().add(votingAnswer);
      } else {
        votingChoice.setVotingAnswers(new HashSet<>(Collections.singleton(votingAnswer)));
      }

      if (votingQuestion.getVotingChoices() != null) {
        votingQuestion.getVotingChoices().add(votingChoice);
      } else {
        votingQuestion.setVotingChoices(new HashSet<>(Collections.singleton(votingChoice)));
      }
    }
  }

}
