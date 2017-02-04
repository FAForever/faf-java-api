package com.faforever.api.leaderboard;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface GlobalLeaderboardRepository extends Repository<GlobalLeaderboardEntry, Integer> {

  @Query(value = "SELECT" +
      "    global_rating.id," +
      "    login.login," +
      "    global_rating.mean," +
      "    global_rating.deviation," +
      "    global_rating.numGames," +
      "    @s \\:= @s + 1 rank" +
      "  FROM global_rating JOIN login on login.id = global_rating.id," +
      "    (SELECT @s \\:= 0) AS s" +
      "  WHERE is_active = 1" +
      "  ORDER BY round(mean - 3 * deviation) DESC", nativeQuery = true)
  List<GlobalLeaderboardEntry> getLeaderboard();
}
