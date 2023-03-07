package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepGameData.sql")
public class GameElideTest extends AbstractIntegrationTest {

  @Test
  @WithAnonymousUser
  public void filterExistingGameByVictoryCondition() throws Exception {
    mockMvc.perform(
      get("/data/game")
        .queryParam("filter", "victoryCondition==DEMORALIZATION")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(1)))
      .andExpect(jsonPath("$.data[0].attributes.victoryCondition", is("DEMORALIZATION")))
      .andExpect(jsonPath("$.data[0].attributes.replayAvailable", is(false)));
  }

  @Test
  @WithAnonymousUser
  public void filterNonExistingGameByVictoryCondition() throws Exception {
    mockMvc.perform(
      get("/data/game")
        .queryParam("filter", "victoryCondition==SANDBOX")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(0)));
  }

  @Test
  @WithAnonymousUser
  public void filterExistingGameReviewsSummaryAverageScore() throws Exception {
    mockMvc.perform(
      get("/data/game")
        .queryParam("filter", "reviewsSummary.averageScore=gt=3.333")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(1)));
  }

  @Test
  @WithAnonymousUser
  public void filterNonExistingGameReviewsSummaryAverageScore() throws Exception {
    mockMvc.perform(
      get("/data/game")
        .queryParam("filter", "reviewsSummary.averageScore=gt=3.334")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(0)));
  }
}

