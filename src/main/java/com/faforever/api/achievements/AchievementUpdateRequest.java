package com.faforever.api.achievements;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AchievementUpdateRequest {

  public enum Operation {
    REVEAL, UNLOCK, INCREMENT, SET_STEPS_AT_LEAST
  }

  private String achievementId;
  private Operation operation;
  private int steps;

  public AchievementUpdateRequest() {
  }

  public AchievementUpdateRequest(String achievementId, Operation operation, int steps) {
    this.achievementId = achievementId;
    this.operation = operation;
    this.steps = steps;
  }
}
