package com.faforever.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class EventUpdateRequest {

  private int playerId;
  private String eventId;
  @Min(0)
  private int count;

}
