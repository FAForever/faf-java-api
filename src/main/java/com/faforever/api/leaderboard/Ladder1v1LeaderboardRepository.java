package com.faforever.api.leaderboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface Ladder1v1LeaderboardRepository extends Repository<Ladder1v1LeaderboardEntry, Integer> {

  @Query(value = "SELECT" +
    "    rating.id," +
    "    login.login," +
    "    rating.mean," +
    "    rating.deviation," +
    "    rating.num_games," +
    "    rating.win_games," +
    "    rating.ranking `rank`" +
    "  FROM ladder1v1_rating_rank_view rating JOIN login on login.id = rating.id" +
    "  ORDER BY rating.rating DESC LIMIT ?#{#pageable.offset},?#{#pageable.pageSize}",
    countQuery = "SELECT count(*) FROM ladder1v1_rating_rank_view", nativeQuery = true)
  Page<Ladder1v1LeaderboardEntry> getLeaderboardByPage(Pageable pageable);

  @Query(value = "SELECT" +
    "                 rating.id," +
    "                 login.login," +
    "                 rating.mean," +
    "                 rating.deviation," +
    "                 rating.num_games," +
    "                 rating.win_games," +
    "                 rating.ranking `rank`" +
    "  FROM ladder1v1_rating_rank_view rating JOIN login on login.id = rating.id" +
    "  WHERE login.id = :playerId", nativeQuery = true)
  Ladder1v1LeaderboardEntry findByPlayerId(@Param("playerId") int playerId);
}
