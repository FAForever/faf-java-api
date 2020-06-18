package com.faforever.api.leaderboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface GlobalLeaderboardRepository extends Repository<GlobalLeaderboardEntry, Integer> {

  @Query(value = "SELECT" +
    "    global_rating.id," +
    "    login.login," +
    "    global_rating.mean," +
    "    global_rating.deviation," +
    "    global_rating.numGames," +
    "    @s \\:= @s + 1 rank" +
    "  FROM global_rating JOIN login on login.id = global_rating.id," +
    "    (SELECT @s \\:= ?#{#pageable.offset}) AS s" +
    "  WHERE is_active = 1" +
    "   AND login.id NOT IN (" +
    "     SELECT player_id FROM ban" +
    "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
    "  ) " +
    "  ORDER BY rating DESC LIMIT ?#{#pageable.offset},?#{#pageable.pageSize}",
    countQuery = "SELECT count(*) FROM global_rating WHERE is_active = 1 AND global_rating.numGames > 0" +
      "   AND id NOT IN (" +
      "     SELECT player_id FROM ban" +
      "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
      "       AND (1=1 OR -1 IN (?,?,?))" +
      "  ) -- Dummy placeholder for pageable, prevents 'Unknown parameter position': ?,?,?", nativeQuery = true)
  Page<GlobalLeaderboardEntry> getLeaderboardByPage(Pageable pageable);

  @Query(value = "SELECT" +
    "    global_rating.id," +
    "    login.login," +
    "    global_rating.mean," +
    "    global_rating.deviation," +
    "    global_rating.numGames," +
    "    @s \\:= @s + 1 rank" +
    "  FROM global_rating JOIN login on login.id = global_rating.id," +
    "    (SELECT @s \\:= ?#{#pageable.offset}) AS s" +
    "  WHERE is_active = 1 AND global_rating.numGames > 0" +
    "   AND login.id NOT IN (" +
    "     SELECT player_id FROM ban" +
    "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
    "  ) " +
    "   AND login.login LIKE ?#{#playerNameRegex}"+
    "  ORDER BY rating DESC LIMIT ?#{#pageable.offset},?#{#pageable.pageSize}",
    countQuery = "SELECT count(*) FROM global_rating JOIN login on login.id = global_rating.id WHERE is_active = 1 AND global_rating.numGames > 0" +
      "   AND global_rating.id NOT IN (" +
      "     SELECT player_id FROM ban" +
      "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
      "       AND (1=1 OR -1 IN (?,?,?))" +
      "  ) "+
      "  AND login.login LIKE ? -- Dummy placeholder for pageable, prevents 'Unknown parameter position': ?,?,?",
    nativeQuery = true)
  Page<GlobalLeaderboardEntry> getLeaderboardByPageAndPlayerNameMatchesRegex(Pageable pageable, @Param("playerNameRegex") String playerRegex);

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
    "   AND login.id NOT IN (" +
    "     SELECT player_id FROM ban" +
    "     WHERE (expires_at is null or expires_at > NOW()) AND revoke_time IS NULL" +
    "  ) " +
    "ORDER BY rating DESC) as leaderboard WHERE id = :playerId", nativeQuery = true)
  GlobalLeaderboardEntry findByPlayerId(@Param("playerId") int playerId);
}
