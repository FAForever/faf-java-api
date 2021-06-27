package com.faforever.api.event;

import com.faforever.api.data.domain.Event;
import com.faforever.api.data.domain.PlayerEvent;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerService;
import com.google.common.base.MoreObjects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsService {

  private final EventRepository eventRepository;
  private final PlayerService playerService;
  private final PlayerEventRepository playerEventRepository;

  UpdatedEventResponse increment(int playerId, String eventId, int steps) {
    // ensure player exists or throw API error instead
    playerService.getById(playerId);

    Event event = eventRepository.findById(eventId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, eventId)));

    PlayerEvent playerEvent = getOrCreatePlayerEvent(playerId, event);

    int currentSteps = MoreObjects.firstNonNull(playerEvent.getCurrentCount(), 0);
    int newCurrentCount = currentSteps + steps;

    playerEvent.setCurrentCount(newCurrentCount);
    playerEventRepository.save(playerEvent);

    return new UpdatedEventResponse(playerEvent.getId(), eventId, newCurrentCount);
  }

  private PlayerEvent getOrCreatePlayerEvent(int playerId, Event event) {
    return playerEventRepository.findOneByEventIdAndPlayerId(event.getId(), playerId)
      .orElseGet(() ->
        {
          log.debug("No event found for event id '{}' and player id '{}'. Creating new event.", event.getId(), playerId);
          return new PlayerEvent()
            .setPlayerId(playerId)
            .setEvent(event);
        }
      );
  }
}
