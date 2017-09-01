package com.faforever.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class EventUpdateRequest {

  private int playerId;
  private String eventId;
  private int count;

}
