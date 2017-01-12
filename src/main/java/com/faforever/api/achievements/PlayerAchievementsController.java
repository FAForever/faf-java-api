package com.faforever.api.achievements;

import com.faforever.api.error.ProgrammingError;
import com.faforever.api.user.FafUserDetails;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/player_achievements")
public class PlayerAchievementsController {

  private final AchievementsService achievementsService;

  @Inject
  public PlayerAchievementsController(AchievementsService achievementsService) {
    this.achievementsService = achievementsService;
  }

  @RequestMapping(method = RequestMethod.PATCH)
  @ApiOperation(value = "Updates the state and progress of one or multiple achievements.")
  public List<AchievementUpdateResponse> update(@RequestBody AchievementUpdateRequest[] updateRequests,
                                                @AuthenticationPrincipal FafUserDetails userDetails) {
    int playerId = userDetails.getId();
    return Arrays.stream(updateRequests).map(achievementUpdateRequest -> {
      switch (achievementUpdateRequest.getOperation()) {
        case REVEAL:
          throw new UnsupportedOperationException("REVEAL is not yet implemented");
        case UNLOCK:
          return achievementsService.unlock(achievementUpdateRequest.getAchievementId(), playerId);
        case INCREMENT:
          return achievementsService.increment(achievementUpdateRequest.getAchievementId(), achievementUpdateRequest.getSteps(), playerId);
        case SET_STEPS_AT_LEAST:
          return achievementsService.setStepsAtLeast(achievementUpdateRequest.getAchievementId(), achievementUpdateRequest.getSteps(), playerId);
        default:
          throw new ProgrammingError("Uncovered update type: " + achievementUpdateRequest.getOperation());
      }
    }).collect(Collectors.toList());
  }
}
