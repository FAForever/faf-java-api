package com.faforever.api.achievements;

import com.faforever.api.data.domain.AchievementState;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
class UpdatedAchievementResponse {

  private final String achievementId;
  private final Integer currentSteps;
  private final AchievementState state;
  private final boolean newlyUnlocked;

  UpdatedAchievementResponse(String achievementId, boolean newlyUnlocked, AchievementState state) {
    this(achievementId, newlyUnlocked, state, null);
  }

  UpdatedAchievementResponse(String achievementId, boolean newlyUnlocked, AchievementState state, @Nullable Integer currentSteps) {
    this.achievementId = achievementId;
    this.currentSteps = currentSteps;
    this.state = state;
    this.newlyUnlocked = newlyUnlocked;
  }
}
