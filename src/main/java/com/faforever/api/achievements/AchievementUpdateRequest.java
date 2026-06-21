package com.faforever.api.achievements;

import io.swagger.v3.oas.annotations.media.Schema;

record AchievementUpdateRequest(
  int playerId,
  String achievementId,
  Operation operation,
  @Schema(description = "Required for INCREMENT and SET_STEPS_AT_LEAST; ignored (and may be null/omitted) for REVEAL and UNLOCK.", nullable = true)
  Integer steps
) {
  public enum Operation {
    REVEAL, UNLOCK, INCREMENT, SET_STEPS_AT_LEAST
  }
}
