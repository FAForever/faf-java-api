package com.faforever.api.voting;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Vote;
import com.faforever.api.game.GamePlayerStatsRepository;
import com.faforever.api.player.PlayerRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

public class VotingServiceTest {
  private VotingService instance;
  @Mock
  private VoteRepository voteRepository;
  @Mock
  private VotingAnswerRepository votingAnswerRepository;
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
    instance = new VotingService(voteRepository, votingAnswerRepository, votingSubjectRepository, gamePlayerStatsRepository, votingChoiceRepository, playerRepository);
  }

  @Ignore
  @Test
  public void saveVote() {
    Vote vote = new Vote();
    Player player = new Player();
    instance.saveVote(vote, player);
  }
}
