package com.faforever.api.league;

import com.faforever.api.config.LeagueDatasourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/truncateTables.sql", config = @SqlConfig(dataSource = LeagueDatasourceConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatasourceConfig.LEAGUE_TRANSACTION_MANAGER))
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/prepLeagueData.sql", config = @SqlConfig(dataSource = LeagueDatasourceConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatasourceConfig.LEAGUE_TRANSACTION_MANAGER))
class LeagueSeasonElideTest extends LeagueAbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeagueSeason() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeason")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(2)));
  }

  @Test
  void anyOneCanReadSpecificLeagueSeason() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeason/1")
    )
      .andExpect(status().isOk());
  }

  @Test
  void noOneCanCreateLeagueSeason() throws Exception {
    mockMvc.perform(
      post("/data/leagueSeason")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeason",
              "attributes": {
                "endDate": "2021-04-21T17:28:39+02:00",
                "startDate": "2021-03-23T16:28:39+01:00"
              },
              "relationships": {
                "leaderboard": {
                  "data": {
                    "type": "leaderboard",
                    "id": "1"
                  }
                },
                "league": {
                  "data": {
                    "type": "league",
                    "id": "1"
                  }
                }
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanUpdateLeagueSeason() throws Exception {
    mockMvc.perform(
      patch("/data/leagueSeason/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeason",
              "id": 1,
              "attributes": {
                "startDate": "2021-03-23T16:28:39+01:00"
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanDeleteLeagueSeason() throws Exception {
    mockMvc.perform(
      delete("/data/leagueSeason/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
