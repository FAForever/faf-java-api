package com.faforever.api.achievements;

import com.faforever.api.data.domain.AchievementState;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
class AchievementUpdateResponse {

  private final Integer currentSteps;
  private final AchievementState state;
  private final boolean newlyUnlocked;

  public AchievementUpdateResponse(boolean newlyUnlocked, AchievementState state) {
    this(newlyUnlocked, state, null);
  }

  AchievementUpdateResponse(boolean newlyUnlocked, AchievementState state, @Nullable Integer currentSteps) {
    this.currentSteps = currentSteps;
    this.state = state;
    this.newlyUnlocked = newlyUnlocked;
  }
}
