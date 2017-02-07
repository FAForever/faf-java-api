package com.faforever.api.leaderboard;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderboardService {

  public static final String LEADERBOARD_RANKED_1V1_CACHE_NAME = "leaderboard1v1";
  public static final String LEADERBOARD_GLOBAL_CACHE_NAME = "leaderboardGlobal";

  private final GlobalLeaderboardRepository globalLeaderboardRepository;
  private final Ladder1v1LeaderboardRepository ladder1v1LeaderboardRepository;

  public LeaderboardService(GlobalLeaderboardRepository globalLeaderboardRepository, Ladder1v1LeaderboardRepository ladder1v1LeaderboardRepository) {
    this.globalLeaderboardRepository = globalLeaderboardRepository;
    this.ladder1v1LeaderboardRepository = ladder1v1LeaderboardRepository;
  }

  @Cacheable(LEADERBOARD_RANKED_1V1_CACHE_NAME)
  public List<Ladder1v1LeaderboardEntry> getLadder1v1Leaderboard() {
    return ladder1v1LeaderboardRepository.getLeaderboard();
  }

  @Cacheable(LEADERBOARD_GLOBAL_CACHE_NAME)
  public List<GlobalLeaderboardEntry> getGlobalLeaderboard() {
    return globalLeaderboardRepository.getLeaderboard();
  }
}
