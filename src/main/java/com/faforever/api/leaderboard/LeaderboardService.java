package com.faforever.api.leaderboard;

import com.faforever.api.utils.DataTypeValidation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.Optional;

@Service
public class LeaderboardService {

  public static final String LEADERBOARD_RANKED_1V1_CACHE_NAME = "LeaderboardService.leaderboard1v1";
  public static final String LEADERBOARD_GLOBAL_CACHE_NAME = "LeaderboardService.leaderboardGlobal";
  private static final int MAX_RESULTS = 10_000;

  private final GlobalLeaderboardRepository globalLeaderboardRepository;
  private final Ladder1v1LeaderboardRepository ladder1v1LeaderboardRepository;

  public LeaderboardService(GlobalLeaderboardRepository globalLeaderboardRepository, Ladder1v1LeaderboardRepository ladder1v1LeaderboardRepository) {
    this.globalLeaderboardRepository = globalLeaderboardRepository;
    this.ladder1v1LeaderboardRepository = ladder1v1LeaderboardRepository;
  }

  @Cacheable(LEADERBOARD_RANKED_1V1_CACHE_NAME)
  public Page<Ladder1v1LeaderboardEntry> getLadder1v1Leaderboard(@Nullable Integer page, @Nullable Integer pageSize) {
    return ladder1v1LeaderboardRepository.getLeaderboardByPage(getPageable(page, pageSize));
  }

  @Cacheable(LEADERBOARD_GLOBAL_CACHE_NAME)
  public Page<GlobalLeaderboardEntry> getGlobalLeaderboard(@Nullable Integer page, @Nullable Integer pageSize) {
    return globalLeaderboardRepository.getLeaderboardByPage(getPageable(page, pageSize));
  }

  public GlobalLeaderboardEntry getGlobalEntry(String playerId) {
    if (DataTypeValidation.isNumeric(playerId)) {
      return globalLeaderboardRepository.findByPlayerId(Integer.parseInt(playerId));
    } else {
      throw new ValidationException();
    }
  }

  public Ladder1v1LeaderboardEntry getLadder1v1Entry(int playerId) {
    return ladder1v1LeaderboardRepository.findByPlayerId(playerId);
  }

  @NotNull
  private Pageable getPageable(@Nullable Integer page, @Nullable Integer pageSize) {
    return new PageRequest(
      Optional.ofNullable(page).map(p -> p - 1).orElse(0),
      Optional.ofNullable(pageSize).orElse(MAX_RESULTS)
    );
  }
}
