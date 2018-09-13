package com.faforever.api.game;

import com.faforever.api.data.domain.GamePlayerStats;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Validity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePlayerStatsRepository extends JpaRepository<GamePlayerStats, Integer> {
  int countByPlayerAndGameValidity(Player player, Validity validity);
}
