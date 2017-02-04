package com.faforever.api.leaderboard;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderboardService {

  private final GlobalLeaderboardRepository globalLeaderboardRepository;
  private final Ranked1v1LeaderboardRepository ranked1v1LeaderboardRepository;

  public LeaderboardService(GlobalLeaderboardRepository globalLeaderboardRepository, Ranked1v1LeaderboardRepository ranked1v1LeaderboardRepository) {
    this.globalLeaderboardRepository = globalLeaderboardRepository;
    this.ranked1v1LeaderboardRepository = ranked1v1LeaderboardRepository;
  }

  public List<Ranked1v1LeaderboardEntry> getRanked1v1Leaderboard() {
    return ranked1v1LeaderboardRepository.getLeaderboard();
  }

  public List<GlobalLeaderboardEntry> getGlobalLeaderboard() {
    return globalLeaderboardRepository.getLeaderboard();
  }
}
