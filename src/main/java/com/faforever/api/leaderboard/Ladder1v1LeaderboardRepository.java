package com.faforever.api.leaderboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

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
    "    (SELECT @s \\:= ?#{#pageable.offset}) AS s" +
    "  WHERE is_active = 1 AND ladder1v1_rating.numGames > 0" +
    "   AND login.id NOT IN (" +
    "     SELECT player_id FROM ban" +
    "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
    "  ) " +
    "  ORDER BY rating DESC LIMIT ?#{#pageable.offset},?#{#pageable.pageSize}",
    countQuery = "SELECT count(*) FROM ladder1v1_rating WHERE is_active = 1 AND ladder1v1_rating.numGames > 0" +
      "   AND id NOT IN (" +
      "     SELECT player_id FROM ban" +
      "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
      "       AND (1=1 OR -1 IN (?,?,?))" +
      "  ) -- Dummy placeholder for pageable, prevents 'Unknown parameter position': ?,?,?",
    nativeQuery = true)
  Page<Ladder1v1LeaderboardEntry> getLeaderboardByPage(Pageable pageable);

  @Query(value = "SELECT" +
    "    ladder1v1_rating.id," +
    "    login.login," +
    "    ladder1v1_rating.mean," +
    "    ladder1v1_rating.deviation," +
    "    ladder1v1_rating.numGames," +
    "    ladder1v1_rating.winGames," +
    "    @s \\:= @s + 1 rank" +
    "  FROM ladder1v1_rating JOIN login on login.id = ladder1v1_rating.id," +
    "    (SELECT @s \\:= ?#{#pageable.offset}) AS s" +
    "  WHERE is_active = 1 AND ladder1v1_rating.numGames > 0" +
    "   AND login.id NOT IN (" +
    "     SELECT player_id FROM ban" +
    "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
    "  ) " +
    "   AND login.login LIKE ?#{#playerNameRegex}"+
    "  ORDER BY rating DESC LIMIT ?#{#pageable.offset},?#{#pageable.pageSize}",
    countQuery = "SELECT count(*) FROM ladder1v1_rating JOIN login on login.id = ladder1v1_rating.id WHERE is_active = 1 AND ladder1v1_rating.numGames > 0" +
      "   AND ladder1v1_rating.id NOT IN (" +
      "     SELECT player_id FROM ban" +
      "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
      "       AND (1=1 OR -1 IN (?,?,?))" +
      "  ) "+
      "  AND login.login LIKE ? -- Dummy placeholder for pageable, prevents 'Unknown parameter position': ?,?,?",
    nativeQuery = true)
  Page<Ladder1v1LeaderboardEntry> getLeaderboardByPageAndPlayerNameMatchesRegex(Pageable pageable, @Param("playerNameRegex") String playerNameRegex);

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
    "   AND login.id NOT IN (" +
    "     SELECT player_id FROM ban" +
    "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
    "  ) " +
    "ORDER BY rating DESC) as leaderboard WHERE id = :playerId", nativeQuery = true)
  Ladder1v1LeaderboardEntry findByPlayerId(@Param("playerId") int playerId);
}
