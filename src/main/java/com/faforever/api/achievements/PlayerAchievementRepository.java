package com.faforever.api.achievements;

import com.faforever.api.data.domain.PlayerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement, String> {

  Optional<PlayerAchievement> findOneByAchievementIdAndPlayerId(String achievementId, int playerId);
}
