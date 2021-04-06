package com.faforever.api.league;

import com.faforever.api.config.LeagueDatasourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/truncateTables.sql", config = @SqlConfig(dataSource = LeagueDatasourceConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatasourceConfig.LEAGUE_TRANSACTION_MANAGER))
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/prepLeagueData.sql", config = @SqlConfig(dataSource = LeagueDatasourceConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatasourceConfig.LEAGUE_TRANSACTION_MANAGER))
class LeaderboardElideTest extends LeagueAbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeaderboard() throws Exception {
    mockMvc.perform(
      get("/data/leagueLeaderboard")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(2)));
  }

  @Test
  void anyOneCanReadSpecificLeaderboard() throws Exception {
    mockMvc.perform(
      get("/data/leagueLeaderboard/1")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.technicalName", is("leaderboard1")));
  }

  @Test
  void noOneCanCreateLeaderboard() throws Exception {
    mockMvc.perform(
      post("/data/leagueLeaderboard")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueLeaderboard",
              "id": "100",
              "attributes": {
                "technicalName": "ForbiddenLeaderboard"
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanUpdateLeaderboard() throws Exception {
    mockMvc.perform(
      patch("/data/leagueLeaderboard/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueLeaderboard",
              "id": 1,
              "attributes": {
                "technicalName": "ForbiddenUpdateLeaderboard"
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanDeleteLeaderboard() throws Exception {
    mockMvc.perform(
      delete("/data/leagueLeaderboard/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
