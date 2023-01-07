package com.faforever.api.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;

import static com.faforever.api.config.RabbitConfiguration.QUEUE_EVENT;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EventsController {

  private final EventsService eventsService;

  @RabbitListener(queues = QUEUE_EVENT)
  public void update(EventUpdateRequest request) {
    log.trace("Received EventUpdateRequest: {}", request);
    eventsService.increment(request.playerId(), request.eventId(), request.count());
  }

}
