package com.faforever.api.game;

import com.faforever.api.data.domain.GamePlayerStats;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Validity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GamePlayerStatsRepository extends JpaRepository<GamePlayerStats, Integer> {
  int countByPlayerAndGameValidity(Player player, Validity validity);

  boolean existsByGameIdAndPlayerId(int gameId, int playerId);

  /**
   * Counts the desync reports that the lobby server has recorded for a player in a game (table {@code game_desync}).
   */
  @Query(value = "SELECT COUNT(*) FROM game_desync WHERE game_id = :gameId AND player_id = :playerId", nativeQuery = true)
  long countGpgnetDesyncReports(@Param("gameId") int gameId, @Param("playerId") int playerId);
}
