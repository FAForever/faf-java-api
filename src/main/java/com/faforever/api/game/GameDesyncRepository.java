package com.faforever.api.game;

import com.faforever.api.data.domain.GameDesync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GameDesyncRepository extends JpaRepository<GameDesync, Integer> {

  /**
   * Marks the desync report of a player as uploaded. The lobby server records desyncs reported via GPGNet in the same
   * table, so the row is created if it does not exist yet. This is a single atomic statement, so it is safe against the
   * lobby server inserting the row at the same time.
   */
  @Transactional
  @Modifying
  @Query(value = """
    INSERT INTO game_desync (game_id, player_id, reported_at, report_uploaded_at)
    VALUES (:gameId, :playerId, NOW(3), NOW(3))
    ON DUPLICATE KEY UPDATE report_uploaded_at = NOW(3)
    """, nativeQuery = true)
  void markReportUploaded(@Param("gameId") int gameId, @Param("playerId") int playerId);
}
