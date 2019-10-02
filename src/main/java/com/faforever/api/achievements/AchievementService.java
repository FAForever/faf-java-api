package com.faforever.api.achievements;

import com.faforever.api.data.domain.Achievement;
import com.faforever.api.data.domain.AchievementState;
import com.faforever.api.data.domain.AchievementType;
import com.faforever.api.data.domain.PlayerAchievement;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.google.common.base.MoreObjects;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_INCREMENTAL;
import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_STANDARD;

@Service
public class AchievementService {

  private final AchievementRepository achievementRepository;
  private final PlayerAchievementRepository playerAchievementRepository;

  public AchievementService(AchievementRepository achievementRepository, PlayerAchievementRepository playerAchievementRepository) {
    this.achievementRepository = achievementRepository;
    this.playerAchievementRepository = playerAchievementRepository;
  }

  UpdatedAchievementResponse increment(int playerId, String achievementId, int steps) {
    return updateSteps(playerId, achievementId, steps, (currentSteps, newSteps) -> currentSteps + newSteps);
  }

  private UpdatedAchievementResponse updateSteps(int playerId, String achievementId, int steps, BiFunction<Integer, Integer, Integer> stepsFunction) {
    Achievement achievement = achievementRepository.getOne(achievementId);
    if (achievement.getType() != AchievementType.INCREMENTAL) {
      throw new ApiException(new Error(ACHIEVEMENT_NOT_INCREMENTAL, achievementId));
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
    Achievement achievement = achievementRepository.getOne(achievementId);
    if (achievement.getType() != AchievementType.STANDARD) {
      throw new ApiException(new Error(ACHIEVEMENT_NOT_STANDARD, achievementId));
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
