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
    "     WHERE (expires_at is null or expires_at > NOW()) AND (revoke_time IS NULL OR revoke_time > NOW())" +
    "  ) " +
    "  ORDER BY round(mean - 3 * deviation) DESC LIMIT ?#{#pageable.offset},?#{#pageable.pageSize}",
    countQuery = "SELECT count(*) FROM ladder1v1_rating WHERE is_active = 1 AND ladder1v1_rating.numGames > 0" +
      "   AND id NOT IN (" +
      "     SELECT player_id FROM ban" +
      "     WHERE (expires_at is null or expires_at > NOW()) AND (revoke_time IS NULL OR revoke_time > NOW())" +
      "  ) -- Dummy placeholder for pageable, prevents 'Unknown parameter position': ?,?,?", nativeQuery = true)
  Page<GlobalLeaderboardEntry> getLeaderboardByPage(Pageable pageable);

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
    "     WHERE (expires_at is null or expires_at <= NOW()) AND (revoke_time IS NULL OR revoke_time > NOW())" +
    "  ) " +
    "ORDER BY round(mean - 3 * deviation) DESC) as leaderboard WHERE id = :playerId", nativeQuery = true)
  GlobalLeaderboardEntry findByPlayerId(@Param("playerId") int playerId);
}
