package com.faforever.api.leaderboard;

import com.google.common.collect.ImmutableMap;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/leaderboards")
public class LeaderboardController {
  private final LeaderboardService leaderboardService;

  public LeaderboardController(LeaderboardService leaderboardService) {
    this.leaderboardService = leaderboardService;
  }

  @RequestMapping(path = "/ladder1v1")
  @Async
  public CompletableFuture<JsonApiDocument> getLadder1v1() {
    List<Resource> values = leaderboardService.getLadder1v1Leaderboard().stream()
        .map(entry -> new Resource("ladder1v1LeaderboardEntry", String.valueOf(entry.getId()),
            ImmutableMap.<String, Object>builder()
                .put("name", entry.getPlayerName())
                .put("mean", entry.getMean())
                .put("deviation", entry.getDeviation())
                .put("numGames", entry.getNumGames())
                .put("wonGames", entry.getWonGames())
                .put("rank", entry.getRank())
                .put("rating", entry.getMean() - 3 * entry.getDeviation())
                .build(),
            null, null, null))
        .collect(Collectors.toList());

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(values)));
  }

  @RequestMapping(path = "/global")
  @Async
  public CompletableFuture<JsonApiDocument> getGlobal() {
    List<Resource> values = leaderboardService.getGlobalLeaderboard().stream()
        .map(entry -> new Resource("globalLeaderboardEntry", String.valueOf(entry.getId()),
            ImmutableMap.<String, Object>builder()
                .put("name", entry.getPlayerName())
                .put("mean", entry.getMean())
                .put("deviation", entry.getDeviation())
                .put("numGames", entry.getNumGames())
                .put("rank", entry.getRank())
                .put("rating", entry.getMean() - 3 * entry.getDeviation())
                .build(),
            null, null, null))
        .collect(Collectors.toList());

    return CompletableFuture.completedFuture(new JsonApiDocument(new Data<>(values)));
  }
}
