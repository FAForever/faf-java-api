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

  void increment(int playerId, String eventId, int steps) {
    log.debug("Increment event id {} for player id {} by {} steps", eventId, playerId, steps);

    BiFunction<Integer, Integer, Integer> stepsFunction = (currentSteps, newSteps) -> currentSteps + newSteps;
    playerService.getById(playerId);
    Event event = eventRepository.findById(eventId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, eventId)));

    PlayerEvent playerEvent = getOrCreatePlayerEvent(playerId, event);

    int currentSteps1 = MoreObjects.firstNonNull(playerEvent.getCurrentCount(), 0);
    int newCurrentCount = stepsFunction.apply(currentSteps1, steps);

    playerEvent.setCurrentCount(newCurrentCount);
    playerEventRepository.save(playerEvent);
  }

  private PlayerEvent getOrCreatePlayerEvent(int playerId, Event event) {
    return playerEventRepository.findOneByEventIdAndPlayerId(event.getId(), playerId)
      .orElseGet(() -> new PlayerEvent()
        .setPlayerId(playerId)
        .setEvent(event));
  }
}
