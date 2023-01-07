package com.faforever.api.achievements;

import com.faforever.api.data.domain.Achievement;
import com.faforever.api.data.domain.AchievementState;
import com.faforever.api.data.domain.AchievementType;
import com.faforever.api.data.domain.PlayerAchievement;
import com.faforever.api.error.ApiException;
import com.google.common.base.MoreObjects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_INCREMENTAL;
import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_STANDARD;
import static com.faforever.api.error.ErrorCode.ENTITY_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

  private final AchievementRepository achievementRepository;
  private final PlayerAchievementRepository playerAchievementRepository;

  void increment(int playerId, String achievementId, int steps) {
    log.debug("Increment achievement id {} for player id {} by {} steps", achievementId, playerId, steps);
    updateSteps(playerId, achievementId, steps, Integer::sum);
  }

  private void updateSteps(int playerId, String achievementId, int steps, BiFunction<Integer, Integer, Integer> stepsFunction) {
    Achievement achievement = achievementRepository.findById(achievementId)
      .orElseThrow(() -> ApiException.of(ENTITY_NOT_FOUND, achievementId));

    if (achievement.getType() != AchievementType.INCREMENTAL) {
      throw ApiException.of(ACHIEVEMENT_NOT_INCREMENTAL, achievementId);
    }

    PlayerAchievement playerAchievement = getOrCreatePlayerAchievement(playerId, achievement, AchievementState.REVEALED);

    int currentSteps = MoreObjects.firstNonNull(playerAchievement.getCurrentSteps(), 0);
    int newCurrentSteps = stepsFunction.apply(currentSteps, steps);

    if (newCurrentSteps >= achievement.getTotalSteps()) {
      playerAchievement.setState(AchievementState.UNLOCKED);
      playerAchievement.setCurrentSteps(achievement.getTotalSteps());
    } else {
      playerAchievement.setCurrentSteps(newCurrentSteps);
    }

    playerAchievementRepository.save(playerAchievement);
  }

  private PlayerAchievement getOrCreatePlayerAchievement(int playerId, Achievement achievement, AchievementState initialState) {
    return playerAchievementRepository.findOneByAchievementIdAndPlayerId(achievement.getId(), playerId)
        .orElseGet(() -> new PlayerAchievement()
            .setPlayerId(playerId)
            .setAchievement(achievement)
            .setState(initialState));
  }

  void setStepsAtLeast(int playerId, String achievementId, int steps) {
    log.debug("Updating achievement id {} for player id {} to minimum {} steps", achievementId, playerId, steps);
    updateSteps(playerId, achievementId, steps, Math::max);
  }

  void unlock(int playerId, String achievementId) {
    Achievement achievement = achievementRepository.findById(achievementId)
      .orElseThrow(() -> ApiException.of(ENTITY_NOT_FOUND, achievementId));

    if (achievement.getType() != AchievementType.STANDARD) {
      throw ApiException.of(ACHIEVEMENT_NOT_STANDARD, achievementId);
    }

    PlayerAchievement playerAchievement = getOrCreatePlayerAchievement(playerId, achievement, AchievementState.REVEALED);

    boolean newlyUnlocked = playerAchievement.getState() != AchievementState.UNLOCKED;

    if (newlyUnlocked) {
      log.info("Player id {} unlocked achievement {}", playerId, playerAchievement.getAchievement().getName());
      playerAchievement.setState(AchievementState.UNLOCKED);
      playerAchievementRepository.save(playerAchievement);
    }
  }
}
