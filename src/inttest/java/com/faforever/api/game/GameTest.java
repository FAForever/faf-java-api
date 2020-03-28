package com.faforever.api.game;

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

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepGameData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanGameData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanMapData.sql")
public class GameTest extends AbstractIntegrationTest {

  @Test
  @WithAnonymousUser
  public void filterExistingGameByVictoryCondition() throws Exception {
    mockMvc.perform(
      get("/data/game")
        .queryParam("filter", "victoryCondition==DEMORALIZATION")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(1)))
      .andExpect(jsonPath("$.data[0].attributes.victoryCondition", is("DEMORALIZATION")));
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
}

