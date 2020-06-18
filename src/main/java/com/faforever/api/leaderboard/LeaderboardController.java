package com.faforever.api.leaderboard;

import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.NotFoundApiException;
import com.google.common.collect.ImmutableMap;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Meta;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
  public CompletableFuture<JsonApiDocument> getLadder(@RequestParam(value = "page[number]", required = false) Integer page,
                                                      @RequestParam(value = "page[size]", required = false) Integer pageSize,
                                                      @ApiParam("Only players that have the following pattern in their name.")
                                                      @RequestParam(value = "playerNameMatchesRegex", required = false) String playerNameMatchesRegex) {
    Page<Ladder1v1LeaderboardEntry> ladder1v1LeaderboardEntries;
    if(playerNameMatchesRegex == null){
      ladder1v1LeaderboardEntries = leaderboardService.getLadder1v1Leaderboard(page, pageSize);
    }else {
      ladder1v1LeaderboardEntries = leaderboardService.getLadder1v1LeaderboardAndFilterPlayerByRegex(page, pageSize, playerNameMatchesRegex);
    }
    List<Resource> values = StreamSupport.stream(
      ladder1v1LeaderboardEntries.spliterator(), false)
      .map(entry -> new Resource(LADDER_1V1_LEADERBOARD_ENTRY, String.valueOf(entry.getId()),
        ImmutableMap.<String, Object>builder()
          .put("name", entry.getPlayerName())
          .put("mean", entry.getMean())
          .put("deviation", entry.getDeviation())
          .put("numGames", entry.getNumGames())
          .put("wonGames", entry.getWonGames())
          .put("rank", entry.getRank())
          .put("rating", (int) (entry.getMean() - 3 * entry.getDeviation()))
          .build(),
        null, null, null))
      .collect(Collectors.toList());

    JsonApiDocument value = new JsonApiDocument(new Data<>(values));
    value.setMeta(new Meta(ImmutableMap
      .of("totalPages", ladder1v1LeaderboardEntries.getTotalPages(),
        "totalRecords", ladder1v1LeaderboardEntries.getTotalElements())
    ));
    return CompletableFuture.completedFuture(value);
  }

  @Async
  @RequestMapping(path = "/global", method = RequestMethod.GET)
  @ApiOperation("Lists the global leaderboard")
  public CompletableFuture<JsonApiDocument> getGlobal(@RequestParam(value = "page[number]", required = false) Integer page,
                                                      @RequestParam(value = "page[size]", required = false) Integer pageSize,
                                                      @ApiParam("Only players that have the following pattern in their name.")
                                                      @RequestParam(value = "playerNameMatchesRegex", required = false) String playerNameMatchesRegex) {
    Page<GlobalLeaderboardEntry> globalLeaderboard;
    if(playerNameMatchesRegex == null){
      globalLeaderboard = leaderboardService.getGlobalLeaderboard(page, pageSize);
    }else {
      globalLeaderboard = leaderboardService.getGlobalLeaderboardAndFilterPlayerByRegex(page, pageSize, playerNameMatchesRegex);
    }
    List<Resource> values = StreamSupport.stream(
      globalLeaderboard.spliterator(), false)
      .map(entry -> new Resource(GLOBAL_LEADERBOARD_ENTRY, String.valueOf(entry.getId()),
        ImmutableMap.<String, Object>builder()
          .put("name", entry.getPlayerName())
          .put("mean", entry.getMean())
          .put("deviation", entry.getDeviation())
          .put("numGames", entry.getNumGames())
          .put("rank", entry.getRank())
          .put("rating", (int) (entry.getMean() - 3 * entry.getDeviation()))
          .build(),
        null, null, null))
      .collect(Collectors.toList());

    JsonApiDocument value = new JsonApiDocument(new Data<>(values));
    value.setMeta(new Meta(ImmutableMap
      .of("totalPages", globalLeaderboard.getTotalPages(),
          "totalRecords", globalLeaderboard.getTotalElements())
    ));
    return CompletableFuture.completedFuture(value);
  }

  @Async
  @RequestMapping(path = "/ladder1v1/{playerId}", method = RequestMethod.GET)
  @ApiOperation("Lists the ladder1v1 leaderboard for the specified player")
  public CompletableFuture<JsonApiDocument> getSingleLadder1v1(@PathVariable("playerId") Integer playerId) {
    Ladder1v1LeaderboardEntry entry = leaderboardService.getLadder1v1Entry(playerId);
    if (entry == null) {
      throw new NotFoundApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, playerId));
    }

    Resource resource = new Resource(LADDER_1V1_LEADERBOARD_ENTRY, playerId.toString(), ImmutableMap.<String, Object>builder()
      .put("name", entry.getPlayerName())
      .put("mean", entry.getMean())
      .put("deviation", entry.getDeviation())
      .put("numGames", entry.getNumGames())
      .put("wonGames", entry.getWonGames())
      .put("rank", entry.getRank())
      .put("rating", (int) (entry.getMean() - 3 * entry.getDeviation()))
      .build(),
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

    Resource resource = new Resource(GLOBAL_LEADERBOARD_ENTRY, playerId.toString(), ImmutableMap.<String, Object>builder()
      .put("name", entry.getPlayerName())
      .put("mean", entry.getMean())
      .put("deviation", entry.getDeviation())
      .put("numGames", entry.getNumGames())
      .put("rank", entry.getRank())
      .put("rating", (int) (entry.getMean() - 3 * entry.getDeviation()))
      .build(),
      null, null, null);

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(resource)));
  }
}
