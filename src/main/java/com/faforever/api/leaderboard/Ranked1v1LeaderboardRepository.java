package com.faforever.api.leaderboard;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface Ranked1v1LeaderboardRepository extends Repository<Ranked1v1LeaderboardEntry, Integer> {
  @Query(value = "SELECT\n" +
      "    ladder1v1_rating.id,\n" +
      "    login.login,\n" +
      "    ladder1v1_rating.mean,\n" +
      "    ladder1v1_rating.deviation,\n" +
      "    ladder1v1_rating.numGames,\n" +
      "    ladder1v1_rating.winGames,\n" +
      "    @s \\:= @s + 1 rank\n" +
      "  FROM ladder1v1_rating JOIN login on login.id = ladder1v1_rating.id\n," +
      "    (SELECT @s \\:= 0) AS s\n" +
      "  WHERE is_active = 1\n" +
      "  ORDER BY round(mean - 3 * deviation) DESC", nativeQuery = true)
  List<Ranked1v1LeaderboardEntry> getLeaderboard();
}
