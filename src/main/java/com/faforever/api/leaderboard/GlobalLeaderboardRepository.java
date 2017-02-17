package com.faforever.api.leaderboard;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

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

  @Query(value = "SELECT * FROM (SELECT\n" +
      "                 global_rating.id,\n" +
      "                 login.login,\n" +
      "                 global_rating.mean,\n" +
      "                 global_rating.deviation,\n" +
      "                 global_rating.numGames,\n" +
      "                 @s \\:= @s + 1 rank\n" +
      "FROM global_rating JOIN login on login.id = global_rating.id,\n" +
      "(SELECT @s \\:= 0) AS s\n" +
      "WHERE is_active = 1\n" +
      "ORDER BY round(mean - 3 * deviation) DESC) as leaderboard WHERE id = :playerId", nativeQuery = true)
  GlobalLeaderboardEntry findByPlayerId(@Param("playerId") int playerId);
}
