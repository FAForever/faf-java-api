package com.faforever.api.leaderboard;

import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LeaderboardControllerTest {

  private LeaderboardController instance;

  @Mock
  private LeaderboardService leaderboardService;

  @Before
  public void setUp() throws Exception {
    instance = new LeaderboardController(leaderboardService);
  }

  @Test
  public void getLadder1v1() throws Exception {
    when(leaderboardService.getLadder1v1Leaderboard()).thenReturn(Arrays.asList(
        new Ladder1v1LeaderboardEntry().setId(14).setPlayerName("JUnit 14").setMean(1500d).setDeviation(51d).setNumGames((short) 514).setRank(1).setWonGames((short) 270),
        new Ladder1v1LeaderboardEntry().setId(5).setPlayerName("JUnit 5").setMean(1400d).setDeviation(67d).setNumGames((short) 65).setRank(2).setWonGames((short) 32)
    ));

    CompletableFuture<JsonApiDocument> result = instance.getLadder1v1();
    assertThat(result.get(), is(notNullValue()));

    Collection<Resource> resources = result.get().getData().get();
    assertThat(resources, hasSize(2));

    Iterator<Resource> iterator = resources.iterator();
    Resource firstEntry = iterator.next();
    assertThat(firstEntry.getId(), is("14"));
    assertThat(firstEntry.getAttributes().get("name"), is("JUnit 14"));
    assertThat(firstEntry.getAttributes().get("mean"), is(1500d));
    assertThat(firstEntry.getAttributes().get("deviation"), is(51d));
    assertThat(firstEntry.getAttributes().get("numGames"), is((short) 514));
    assertThat(firstEntry.getAttributes().get("wonGames"), is((short) 270));
    assertThat(firstEntry.getAttributes().get("rank"), is(1));
    assertThat(firstEntry.getAttributes().get("rating"), is(1347.0));

    Resource secondEntry = iterator.next();
    assertThat(secondEntry.getId(), is("5"));
    assertThat(secondEntry.getAttributes().get("name"), is("JUnit 5"));
    assertThat(secondEntry.getAttributes().get("mean"), is(1400d));
    assertThat(secondEntry.getAttributes().get("deviation"), is(67d));
    assertThat(secondEntry.getAttributes().get("numGames"), is((short) 65));
    assertThat(secondEntry.getAttributes().get("wonGames"), is((short) 32));
    assertThat(secondEntry.getAttributes().get("rank"), is(2));
    assertThat(secondEntry.getAttributes().get("rating"), is(1199d));
  }

  @Test
  public void getGlobal() throws Exception {
    when(leaderboardService.getGlobalLeaderboard()).thenReturn(Arrays.asList(
        new GlobalLeaderboardEntry().setId(14).setPlayerName("JUnit 14").setMean(1500d).setDeviation(51d).setNumGames((short) 514).setRank(1),
        new GlobalLeaderboardEntry().setId(5).setPlayerName("JUnit 5").setMean(1400d).setDeviation(67d).setNumGames((short) 65).setRank(2)
    ));

    CompletableFuture<JsonApiDocument> result = instance.getGlobal();
    assertThat(result.get(), is(notNullValue()));

    Collection<Resource> resources = result.get().getData().get();
    assertThat(resources, hasSize(2));

    Iterator<Resource> iterator = resources.iterator();
    Resource firstEntry = iterator.next();
    assertThat(firstEntry.getId(), is("14"));
    assertThat(firstEntry.getAttributes().get("name"), is("JUnit 14"));
    assertThat(firstEntry.getAttributes().get("mean"), is(1500d));
    assertThat(firstEntry.getAttributes().get("deviation"), is(51d));
    assertThat(firstEntry.getAttributes().get("numGames"), is((short) 514));
    assertThat(firstEntry.getAttributes().get("rank"), is(1));
    assertThat(firstEntry.getAttributes().get("rating"), is(1347.0));

    Resource secondEntry = iterator.next();
    assertThat(secondEntry.getId(), is("5"));
    assertThat(secondEntry.getAttributes().get("name"), is("JUnit 5"));
    assertThat(secondEntry.getAttributes().get("mean"), is(1400d));
    assertThat(secondEntry.getAttributes().get("deviation"), is(67d));
    assertThat(secondEntry.getAttributes().get("numGames"), is((short) 65));
    assertThat(secondEntry.getAttributes().get("rank"), is(2));
    assertThat(secondEntry.getAttributes().get("rating"), is(1199d));
  }
}
