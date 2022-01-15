package com.faforever.api.achievements;

import com.faforever.api.data.JsonApiMediaType;
import com.faforever.api.security.OAuthScope;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(path = "/achievements")
@RequiredArgsConstructor
public class AchievementsController {

  private final AchievementService achievementService;
  private final AtomicInteger nextUpdateId = new AtomicInteger();

  @ApiOperation(value = "Updates the state and progress of one or multiple achievements.")
  @PreAuthorize("hasScope('" + OAuthScope._WRITE_ACHIEVEMENTS + "')")
  @RequestMapping(value = "/update", method = RequestMethod.PATCH, produces = JsonApiMediaType.JSON_API_MEDIA_TYPE)
  public JsonApiDocument update(@RequestBody AchievementUpdateRequest[] updateRequests) {
    return new JsonApiDocument(new Data<>(Arrays.stream(updateRequests)
      .map(request -> switch (request.operation()) {
        case REVEAL -> throw new UnsupportedOperationException("REVEAL is not yet implemented");
        case UNLOCK -> achievementService.unlock(request.playerId(), request.achievementId());
        case INCREMENT -> achievementService.increment(request.playerId(), request.achievementId(), request.steps());
        case SET_STEPS_AT_LEAST -> achievementService.setStepsAtLeast(request.playerId(), request.achievementId(), request.steps());
      })
      .map(this::toResource)
      .toList()));
  }

  private Resource toResource(UpdatedAchievementResponse updatedAchievementResponse) {
    Builder<String, Object> attributesBuilder = ImmutableMap.<String, Object>builder()
      .put("achievementId", updatedAchievementResponse.achievementId())
      .put("state", updatedAchievementResponse.state())
      .put("newlyUnlocked", updatedAchievementResponse.newlyUnlocked());

    if (updatedAchievementResponse.currentSteps() != null) {
      attributesBuilder.put("currentSteps", updatedAchievementResponse.currentSteps());
    }

    return new Resource("updatedAchievement", String.valueOf(nextUpdateId.getAndIncrement()),
      attributesBuilder.build(), null, null, null);
  }
}
