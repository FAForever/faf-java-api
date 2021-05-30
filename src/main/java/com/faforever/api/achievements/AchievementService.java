package com.faforever.api.achievements;

import com.faforever.api.data.domain.Achievement;
import com.faforever.api.data.domain.AchievementState;
import com.faforever.api.data.domain.AchievementType;
import com.faforever.api.data.domain.PlayerAchievement;
import com.faforever.api.error.ApiException;
import com.google.common.base.MoreObjects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_INCREMENTAL;
import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_STANDARD;
import static com.faforever.api.error.ErrorCode.ENTITY_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AchievementService {

  private final AchievementRepository achievementRepository;
  private final PlayerAchievementRepository playerAchievementRepository;

  UpdatedAchievementResponse increment(int playerId, String achievementId, int steps) {
    return updateSteps(playerId, achievementId, steps, Integer::sum);
  }

  private UpdatedAchievementResponse updateSteps(int playerId, String achievementId, int steps, BiFunction<Integer, Integer, Integer> stepsFunction) {
    Achievement achievement = achievementRepository.findById(achievementId)
      .orElseThrow(() -> ApiException.of(ENTITY_NOT_FOUND, achievementId));

    if (achievement.getType() != AchievementType.INCREMENTAL) {
      throw ApiException.of(ACHIEVEMENT_NOT_INCREMENTAL, achievementId);
    }

    PlayerAchievement playerAchievement = getOrCreatePlayerAchievement(playerId, achievement, AchievementState.REVEALED);

    int currentSteps = MoreObjects.firstNonNull(playerAchievement.getCurrentSteps(), 0);
    int newCurrentSteps = stepsFunction.apply(currentSteps, steps);

    boolean newlyUnlocked = false;

    if (newCurrentSteps >= achievement.getTotalSteps()) {
      playerAchievement.setState(AchievementState.UNLOCKED);
      playerAchievement.setCurrentSteps(achievement.getTotalSteps());
      newlyUnlocked = playerAchievement.getState() != AchievementState.UNLOCKED;
    } else {
      playerAchievement.setCurrentSteps(newCurrentSteps);
    }

    playerAchievementRepository.save(playerAchievement);

    return new UpdatedAchievementResponse(achievementId, newlyUnlocked, playerAchievement.getState(), playerAchievement.getCurrentSteps());
  }

  private PlayerAchievement getOrCreatePlayerAchievement(int playerId, Achievement achievement, AchievementState initialState) {
    return playerAchievementRepository.findOneByAchievementIdAndPlayerId(achievement.getId(), playerId)
        .orElseGet(() -> new PlayerAchievement()
            .setPlayerId(playerId)
            .setAchievement(achievement)
            .setState(initialState));
  }

  UpdatedAchievementResponse setStepsAtLeast(int playerId, String achievementId, int steps) {
    return updateSteps(playerId, achievementId, steps, Math::max);
  }

  UpdatedAchievementResponse unlock(int playerId, String achievementId) {
    Achievement achievement = achievementRepository.findById(achievementId)
      .orElseThrow(() -> ApiException.of(ENTITY_NOT_FOUND, achievementId));

    if (achievement.getType() != AchievementType.STANDARD) {
      throw ApiException.of(ACHIEVEMENT_NOT_STANDARD, achievementId);
    }

    PlayerAchievement playerAchievement = getOrCreatePlayerAchievement(playerId, achievement, AchievementState.REVEALED);

    boolean newlyUnlocked = playerAchievement.getState() != AchievementState.UNLOCKED;

    if (newlyUnlocked) {
      playerAchievement.setState(AchievementState.UNLOCKED);
      playerAchievementRepository.save(playerAchievement);
    }

    return new UpdatedAchievementResponse(achievementId, newlyUnlocked, playerAchievement.getState());
  }
}
