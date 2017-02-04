package com.faforever.api.achievements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
class AchievementUpdateRequest {

  private String achievementId;
  private Operation operation;
  private int steps;

  AchievementUpdateRequest(String achievementId, Operation operation, int steps) {
    this.achievementId = achievementId;
    this.operation = operation;
    this.steps = steps;
  }

  public enum Operation {
    REVEAL, UNLOCK, INCREMENT, SET_STEPS_AT_LEAST
  }
}
