package com.faforever.api.leaderboard;

import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.NotFoundApiException;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/leaderboards")
public class LeaderboardController {
  private static final String GLOBAL_LEADERBOARD_ENTRY = "globalLeaderboardEntry";
  private static final String LADDER_1V1_LEADERBOARD_ENTRY = "ladder1v1LeaderboardEntry";

  private final LeaderboardService leaderboardService;

  public LeaderboardController(LeaderboardService leaderboardService) {
    this.leaderboardService = leaderboardService;
  }

  @Async
  @RequestMapping(path = "/ladder1v1", method = RequestMethod.GET)
  @ApiOperation("Lists the ladder1v1 leaderboard")
  public CompletableFuture<JsonApiDocument> getLadder1v1(@RequestParam(value = "page[number]", required = false) Integer page,
                                                         @RequestParam(value = "page[size]", required = false) Integer pageSize) {
    List<Resource> values = StreamSupport.stream(leaderboardService.getLadder1v1Leaderboard(page, pageSize).spliterator(), false)
      .map(entry -> new Resource(LADDER_1V1_LEADERBOARD_ENTRY, String.valueOf(entry.getId()),
        Map.of(
          "name", entry.getPlayerName(),
          "mean", entry.getMean(),
          "deviation", entry.getDeviation(),
          "numGames", entry.getNumGames(),
          "wonGames", entry.getWonGames(),
          "rank", entry.getRank(),
          "rating", (int) (entry.getMean() - 3 * entry.getDeviation())
        ),
        null, null, null))
      .collect(Collectors.toList());

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(values)));
  }

  @Async
  @RequestMapping(path = "/global", method = RequestMethod.GET)
  @ApiOperation("Lists the global leaderboard")
  public CompletableFuture<JsonApiDocument> getGlobal(@RequestParam(value = "page[number]", required = false) Integer page,
                                                      @RequestParam(value = "page[size]", required = false) Integer pageSize) {
    List<Resource> values = StreamSupport.stream(leaderboardService.getGlobalLeaderboard(page, pageSize).spliterator(), false)
      .map(entry -> new Resource(GLOBAL_LEADERBOARD_ENTRY, String.valueOf(entry.getId()),
        Map.of(
          "name", entry.getPlayerName(),
          "mean", entry.getMean(),
          "deviation", entry.getDeviation(),
          "numGames", entry.getNumGames(),
          "rank", entry.getRank(),
          "rating", (int) (entry.getMean() - 3 * entry.getDeviation())
        ),
        null, null, null))
      .collect(Collectors.toList());

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(values)));
  }

  @Async
  @RequestMapping(path = "/ladder1v1/{playerId}", method = RequestMethod.GET)
  @ApiOperation("Lists the ladder1v1 leaderboard for the specified player")
  public CompletableFuture<JsonApiDocument> getSingleLadder1v1(@PathVariable("playerId") Integer playerId) {
    Ladder1v1LeaderboardEntry entry = leaderboardService.getLadder1v1Entry(playerId);
    if (entry == null) {
      throw new NotFoundApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, playerId));
    }

    Resource resource = new Resource(LADDER_1V1_LEADERBOARD_ENTRY, playerId.toString(), Map.of(
      "name", entry.getPlayerName(),
      "mean", entry.getMean(),
      "deviation", entry.getDeviation(),
      "numGames", entry.getNumGames(),
      "wonGames", entry.getWonGames(),
      "rank", entry.getRank(),
      "rating", (int) (entry.getMean() - 3 * entry.getDeviation())
    ),
      null, null, null);

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(resource)));
  }

  @Async
  @RequestMapping(path = "/global/{playerId}", method = RequestMethod.GET)
  @ApiOperation("Lists the global leaderboard for the specified player")
  public CompletableFuture<JsonApiDocument> getSingleGlobal(@PathVariable("playerId") Integer playerId) {
    GlobalLeaderboardEntry entry = leaderboardService.getGlobalEntry(playerId);
    if (entry == null) {
      throw new NotFoundApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, playerId));
    }

    Resource resource = new Resource(GLOBAL_LEADERBOARD_ENTRY, playerId.toString(), Map.of(
      "name", entry.getPlayerName(),
      "mean", entry.getMean(),
      "deviation", entry.getDeviation(),
      "numGames", entry.getNumGames(),
      "rank", entry.getRank(),
      "rating", (int) (entry.getMean() - 3 * entry.getDeviation())
    ),
      null, null, null);

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(resource)));
  }
}
