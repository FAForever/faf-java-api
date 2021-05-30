package com.faforever.api.achievements;

import com.faforever.api.data.domain.AchievementState;

record UpdatedAchievementResponse(
  String achievementId,
  boolean newlyUnlocked,
  AchievementState state,
  Integer currentSteps
) {
  UpdatedAchievementResponse(String achievementId, boolean newlyUnlocked, AchievementState state) {
    this(achievementId, newlyUnlocked, state, null);
  }
}
