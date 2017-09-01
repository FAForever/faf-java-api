package com.faforever.api.event;

import lombok.Data;

@Data
class UpdatedEventResponse {

  private final String eventId;
  private final Integer currentCount;

}
