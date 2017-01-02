package com.faforever.api.achievements;

import com.faforever.api.data.domain.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement, String> {

  Optional<PlayerAchievement> findOneByAchievementIdAndPlayerId(String achievementId, int playerId);
}
