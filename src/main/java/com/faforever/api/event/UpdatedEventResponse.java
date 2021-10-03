package com.faforever.api.event;


record UpdatedEventResponse(
  int id,
  String eventId,
  Integer currentCount
) {
}
