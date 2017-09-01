package com.faforever.api.achievements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AchievementUpdateRequest {

  private int playerId;
  private String achievementId;
  private Operation operation;
  private int steps;

  public enum Operation {
    REVEAL, UNLOCK, INCREMENT, SET_STEPS_AT_LEAST
  }
}
