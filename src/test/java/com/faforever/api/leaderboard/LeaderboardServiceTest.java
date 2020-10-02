package com.faforever.api.leaderboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeaderboardServiceTest {

  private LeaderboardService instance;

  @Mock
  private GlobalLeaderboardRepository globalLeaderboardRepository;
  @Mock
  private Ladder1v1LeaderboardRepository ladder1v1LeaderboardRepository;

  @BeforeEach
  public void setUp() throws Exception {
    instance = new LeaderboardService(globalLeaderboardRepository, ladder1v1LeaderboardRepository);
  }

  @Test
  public void getLadder1v1Leaderboard() throws Exception {
    Page<Ladder1v1LeaderboardEntry> leaderboard = new PageImpl<>(Collections.emptyList());
    when(ladder1v1LeaderboardRepository.getLeaderboardByPage(PageRequest.of(0, 100))).thenReturn(leaderboard);

    List<Ladder1v1LeaderboardEntry> result = instance.getLadder1v1Leaderboard(1, 100).getContent();

    assertThat(result, is(leaderboard.getContent()));
    verify(ladder1v1LeaderboardRepository).getLeaderboardByPage(PageRequest.of(0, 100));
  }

  @Test
  public void getGlobalLeaderboard() throws Exception {
    Page<GlobalLeaderboardEntry> leaderboard = new PageImpl<>(Collections.emptyList());
    when(globalLeaderboardRepository.getLeaderboardByPage(PageRequest.of(0, 100))).thenReturn(leaderboard);

    List<GlobalLeaderboardEntry> result = instance.getGlobalLeaderboard(1, 100).getContent();

    assertThat(result, is(leaderboard.getContent()));
    verify(globalLeaderboardRepository).getLeaderboardByPage(PageRequest.of(0, 100));
  }
}
