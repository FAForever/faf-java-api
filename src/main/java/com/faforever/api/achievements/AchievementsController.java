package com.faforever.api.achievements;

import com.faforever.api.data.JsonApiMediaType;
import com.faforever.api.error.ProgrammingError;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/achievements")
@RequiredArgsConstructor
public class AchievementsController {

  private static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";
  private final AchievementService achievementService;
  private AtomicInteger nextUpdateId = new AtomicInteger();

  @ApiOperation(value = "Updates the state and progress of one or multiple achievements.")
  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._WRITE_ACHIEVEMENTS + "')")
  @RequestMapping(value = "/update", method = RequestMethod.PATCH, produces = JsonApiMediaType.JSON_API_MEDIA_TYPE)
  public JsonApiDocument update(@RequestBody AchievementUpdateRequest[] updateRequests) {
    return new JsonApiDocument(new Data<>(Arrays.stream(updateRequests)
        .map(request -> {
          switch (request.getOperation()) {
            case REVEAL:
              throw new UnsupportedOperationException("REVEAL is not yet implemented");
            case UNLOCK:
              return achievementService.unlock(request.getPlayerId(), request.getAchievementId());
            case INCREMENT:
              return achievementService.increment(request.getPlayerId(), request.getAchievementId(), request.getSteps());
            case SET_STEPS_AT_LEAST:
              return achievementService.setStepsAtLeast(request.getPlayerId(), request.getAchievementId(), request.getSteps());
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
        .put("state", updatedAchievementResponse.getState())
        .put("newlyUnlocked", updatedAchievementResponse.isNewlyUnlocked());

    if (updatedAchievementResponse.getCurrentSteps() != null) {
      attributesBuilder.put("currentSteps", updatedAchievementResponse.getCurrentSteps());
    }

    return new Resource("updatedAchievement", String.valueOf(nextUpdateId.getAndIncrement()),
        attributesBuilder.build(), null, null, null);
  }
}
