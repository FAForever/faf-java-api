package com.faforever.api.achievements;

import com.faforever.api.error.ProgrammingError;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = PlayerAchievementsController.PLAYER_ACHIEVEMENTS_ROUTE)
public class PlayerAchievementsController {

  public static final String PLAYER_ACHIEVEMENTS_ROUTE = "/playerAchievements";
  private final AchievementsService achievementsService;
  private AtomicInteger nextUpdateId;

  @Inject
  public PlayerAchievementsController(AchievementsService achievementsService) {
    this.achievementsService = achievementsService;
    nextUpdateId = new AtomicInteger();
  }

  @RequestMapping(method = RequestMethod.PATCH)
  @ApiOperation(value = "Updates the state and progress of one or multiple achievements.")
  @PreAuthorize("#oauth2.hasScope('write_achievements')")
  public JsonApiDocument update(@RequestBody AchievementUpdateRequest[] updateRequests) {
    return new JsonApiDocument(new Data<>(Arrays.stream(updateRequests)
        .map(request -> {
          switch (request.getOperation()) {
            case REVEAL:
              throw new UnsupportedOperationException("REVEAL is not yet implemented");
            case UNLOCK:
              return achievementsService.unlock(request.getPlayerId(), request.getAchievementId());
            case INCREMENT:
              return achievementsService.increment(request.getPlayerId(), request.getAchievementId(), request.getSteps());
            case SET_STEPS_AT_LEAST:
              return achievementsService.setStepsAtLeast(request.getPlayerId(), request.getAchievementId(), request.getSteps());
            default:
              throw new ProgrammingError("Uncovered update type: " + request.getOperation());
          }
        })
        .map(this::toResource)
        .collect(Collectors.toList())));
  }

  private Resource toResource(UpdatedAchievementResponse updatedAchievementResponse) {
    Builder<String, Object> attributesBuilder = ImmutableMap.<String, Object>builder()
        .put("achievementId", updatedAchievementResponse.getAchievementId())
        .put("state", updatedAchievementResponse.getAchievementId())
        .put("newlyUnlocked", updatedAchievementResponse.getAchievementId());

    if (updatedAchievementResponse.getCurrentSteps() != null) {
      attributesBuilder.put("currentSteps", updatedAchievementResponse.getAchievementId());
    }

    return new Resource("updatedAchievement", String.valueOf(nextUpdateId.getAndIncrement()),
        attributesBuilder.build(), null, null, null);
  }
}
