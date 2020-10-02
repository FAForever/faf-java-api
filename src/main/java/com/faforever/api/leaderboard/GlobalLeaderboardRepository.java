package com.faforever.api.leaderboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface GlobalLeaderboardRepository extends Repository<GlobalLeaderboardEntry, Integer> {

  @Query(value = "SELECT" +
    "    rating.id," +
    "    login.login," +
    "    rating.mean," +
    "    rating.deviation," +
    "    rating.num_games," +
    "    rating.ranking as `rank`" +
    "  FROM global_rating_rank_view rating JOIN login on login.id = rating.id" +
    "  ORDER BY rating.rating DESC LIMIT ?#{#pageable.offset},?#{#pageable.pageSize}",
    countQuery = "SELECT count(*) FROM global_rating_rank_view", nativeQuery = true)
  Page<GlobalLeaderboardEntry> getLeaderboardByPage(Pageable pageable);

  @Query(value = "SELECT" +
    "                 rating.id," +
    "                 login.login," +
    "                 rating.mean," +
    "                 rating.deviation," +
    "                 rating.num_games," +
    "                 rating.ranking `rank`" +
    "  FROM global_rating_rank_view rating JOIN login on login.id = rating.id" +
    "  WHERE login.id = :playerId", nativeQuery = true)
  GlobalLeaderboardEntry findByPlayerId(@Param("playerId") int playerId);
}
