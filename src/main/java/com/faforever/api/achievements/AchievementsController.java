package com.faforever.api.achievements;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;

import static com.faforever.api.config.RabbitConfiguration.QUEUE_ACHIEVEMENT;

@RequiredArgsConstructor
@Controller
public class AchievementsController {

  private final AchievementService achievementService;

  @RabbitListener(queues = QUEUE_ACHIEVEMENT)
  public void update(AchievementUpdateRequest request) {
    switch (request.operation()) {
      case REVEAL -> throw new UnsupportedOperationException("REVEAL is not yet implemented");
      case UNLOCK -> achievementService.unlock(request.playerId(), request.achievementId());
      case INCREMENT -> achievementService.increment(request.playerId(), request.achievementId(), request.steps());
      case SET_STEPS_AT_LEAST ->
        achievementService.setStepsAtLeast(request.playerId(), request.achievementId(), request.steps());
    }
  }
}
