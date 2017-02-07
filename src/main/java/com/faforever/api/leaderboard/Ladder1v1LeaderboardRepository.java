package com.faforever.api.leaderboard;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Ladder1v1LeaderboardRepository extends Repository<Ladder1v1LeaderboardEntry, Integer> {
  @Query(value = "SELECT" +
      "    ladder1v1_rating.id," +
      "    login.login," +
      "    ladder1v1_rating.mean," +
      "    ladder1v1_rating.deviation," +
      "    ladder1v1_rating.numGames," +
      "    ladder1v1_rating.winGames," +
      "    @s \\:= @s + 1 rank" +
      "  FROM ladder1v1_rating JOIN login on login.id = ladder1v1_rating.id," +
      "    (SELECT @s \\:= 0) AS s" +
      "  WHERE is_active = 1" +
      "  ORDER BY round(mean - 3 * deviation) DESC", nativeQuery = true)
  List<Ladder1v1LeaderboardEntry> getLeaderboard();

  @Query(value = "SELECT * FROM (SELECT\n" +
      "                 ladder1v1_rating.id,\n" +
      "                 login.login,\n" +
      "                 ladder1v1_rating.mean,\n" +
      "                 ladder1v1_rating.deviation,\n" +
      "                 ladder1v1_rating.numGames,\n" +
      "                 ladder1v1_rating.winGames," +
      "                 @s \\:= @s + 1 rank\n" +
      "FROM ladder1v1_rating JOIN login on login.id = ladder1v1_rating.id,\n" +
      "(SELECT @s \\:= 0) AS s\n" +
      "WHERE is_active = 1\n" +
      "ORDER BY round(mean - 3 * deviation) DESC) as leaderboard WHERE id = :playerId", nativeQuery = true)
  Ladder1v1LeaderboardEntry findByPlayerId(@Param("playerId") int playerId);
}
