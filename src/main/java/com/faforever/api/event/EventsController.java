package com.faforever.api.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;

import static com.faforever.api.config.RabbitConfiguration.QUEUE_EVENT;

@Controller
@RequiredArgsConstructor
public class EventsController {

  private final EventsService eventsService;

  @RabbitListener(queues = QUEUE_EVENT)
  public void update(EventUpdateRequest request) {
      eventsService.increment(request.playerId(), request.eventId(), request.count());
  }

}
