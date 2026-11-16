package com.faforever.api.game;

import com.faforever.api.data.domain.GameDesync;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameDesyncRepository extends JpaRepository<GameDesync, Integer> {
  boolean existsByGameIdAndPlayerId(int gameId, int playerId);
}
