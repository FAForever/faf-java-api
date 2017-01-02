package com.faforever.api.achievements;

import com.faforever.api.data.domain.AchievementDefinition;
import com.faforever.api.data.domain.AchievementState;
import com.faforever.api.data.domain.AchievementType;
import com.faforever.api.data.domain.PlayerAchievement;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.google.common.base.MoreObjects;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.function.BiFunction;

import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_INCREMENTAL;
import static com.faforever.api.error.ErrorCode.ACHIEVEMENT_NOT_STANDARD;

@Service
public class AchievementsService {

  private final AchievementDefinitionRepository achievementDefinitionRepository;
  private final PlayerAchievementRepository playerAchievementRepository;

  @Inject
  public AchievementsService(AchievementDefinitionRepository achievementDefinitionRepository, PlayerAchievementRepository playerAchievementRepository) {
    this.achievementDefinitionRepository = achievementDefinitionRepository;
    this.playerAchievementRepository = playerAchievementRepository;
  }

  AchievementUpdateResponse increment(String achievementId, int steps, int playerId) {
    return updateSteps(achievementId, steps, playerId, (currentSteps, newSteps) -> currentSteps + newSteps);
  }

  private AchievementUpdateResponse updateSteps(String achievementId, int steps, int playerId, BiFunction<Integer, Integer, Integer> stepsFunction) {
    AchievementDefinition achievementDefinition = achievementDefinitionRepository.getOne(achievementId);
    if (achievementDefinition.getType() != AchievementType.INCREMENTAL) {
      throw new ApiException(new Error(ACHIEVEMENT_NOT_INCREMENTAL, achievementId));
    }

    PlayerAchievement playerAchievement = getOrCreatePlayerAchievement(playerId, achievementDefinition, AchievementState.REVEALED);

    int currentSteps = MoreObjects.firstNonNull(playerAchievement.getCurrentSteps(), 0);
    int newCurrentSteps = stepsFunction.apply(currentSteps, steps);

    boolean newlyUnlocked = false;

    if (newCurrentSteps >= achievementDefinition.getTotalSteps()) {
      playerAchievement.setState(AchievementState.UNLOCKED);
      playerAchievement.setCurrentSteps(achievementDefinition.getTotalSteps());
      newlyUnlocked = playerAchievement.getState() != AchievementState.UNLOCKED;
    } else {
      playerAchievement.setCurrentSteps(newCurrentSteps);
    }

    playerAchievementRepository.save(playerAchievement);

    return new AchievementUpdateResponse(newlyUnlocked, playerAchievement.getState(), playerAchievement.getCurrentSteps());
  }

  private PlayerAchievement getOrCreatePlayerAchievement(int playerId, AchievementDefinition achievementDefinition, AchievementState initialState) {
    return playerAchievementRepository.findOneByAchievementIdAndPlayerId(achievementDefinition.getId(), playerId)
        .orElseGet(() -> {
          PlayerAchievement newPlayerAchievement = new PlayerAchievement();
          newPlayerAchievement.setAchievement(achievementDefinition);
          newPlayerAchievement.setState(initialState);
          return newPlayerAchievement;
        });
  }

  AchievementUpdateResponse setStepsAtLeast(String achievementId, int steps, int playerId) {
    return updateSteps(achievementId, steps, playerId, Math::max);
  }

  AchievementUpdateResponse unlock(String achievementId, int playerId) {
    AchievementDefinition achievementDefinition = achievementDefinitionRepository.getOne(achievementId);
    if (achievementDefinition.getType() != AchievementType.STANDARD) {
      throw new ApiException(new Error(ACHIEVEMENT_NOT_STANDARD, achievementId));
    }

    PlayerAchievement playerAchievement = getOrCreatePlayerAchievement(playerId, achievementDefinition, AchievementState.REVEALED);

    boolean newlyUnlocked = playerAchievement.getState() != AchievementState.UNLOCKED;

    if (newlyUnlocked) {
      playerAchievement.setState(AchievementState.UNLOCKED);
      playerAchievementRepository.save(playerAchievement);
    }

    return new AchievementUpdateResponse(newlyUnlocked, playerAchievement.getState());
  }
}
