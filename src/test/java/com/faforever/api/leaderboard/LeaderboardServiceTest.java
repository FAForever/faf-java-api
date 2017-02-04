package com.faforever.api.leaderboard;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LeaderboardServiceTest {

  private LeaderboardService instance;

  @Mock
  private GlobalLeaderboardRepository globalLeaderboardRepository;
  @Mock
  private Ranked1v1LeaderboardRepository ranked1v1LeaderboardRepository;

  @Before
  public void setUp() throws Exception {
    instance = new LeaderboardService(globalLeaderboardRepository, ranked1v1LeaderboardRepository);
  }

  @Test
  public void getRanked1v1Leaderboard() throws Exception {
    List<Ranked1v1LeaderboardEntry> leaderboard = Collections.emptyList();
    when(ranked1v1LeaderboardRepository.getLeaderboard()).thenReturn(leaderboard);

    List<Ranked1v1LeaderboardEntry> result = instance.getRanked1v1Leaderboard();

    assertThat(result, is(leaderboard));
    verify(ranked1v1LeaderboardRepository).getLeaderboard();
  }

  @Test
  public void getGlobalLeaderboard() throws Exception {
    List<GlobalLeaderboardEntry> leaderboard = Collections.emptyList();
    when(globalLeaderboardRepository.getLeaderboard()).thenReturn(leaderboard);

    List<GlobalLeaderboardEntry> result = instance.getGlobalLeaderboard();

    assertThat(result, is(leaderboard));
    verify(globalLeaderboardRepository).getLeaderboard();
  }
}
