package com.faforever.api.achievements;

record AchievementUpdateRequest(
  int playerId,
  String achievementId,
  Operation operation,
  int steps
) {
  public enum Operation {
    REVEAL, UNLOCK, INCREMENT, SET_STEPS_AT_LEAST
  }
}
