package com.faforever.api.event;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

@Validated
record EventUpdateRequest(
  int playerId,
  String eventId,
  @Min(0)
  int count
) {
}
