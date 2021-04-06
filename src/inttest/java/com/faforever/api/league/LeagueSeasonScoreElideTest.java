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
class LeagueSeasonScoreElideTest extends LeagueAbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeagueSeasonScore() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeasonScore")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(8)));
  }

  @Test
  void anyOneCanReadPlayerAndSeasonLeagueSeasonScore() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeasonScore?filter=loginId==1;leagueSeason.id==1")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(1)));
  }

  @Test
  void anyOneCanReadSpecificLeagueSeasonScore() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeasonScore/1")
    )
      .andExpect(status().isOk());
  }

  @Test
  void noOneCanCreateLeagueSeasonScore() throws Exception {
    mockMvc.perform(
      post("/data/leagueSeasonScore")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeasonScore",
              "attributes": {
                "gameCount": 10,
                "loginId": 10,
                "score": 10
              },
              "relationships": {
                "leagueSeason": {
                  "data": {
                    "type": "leagueSeason",
                    "id": "1"
                  }
                },
                "leagueSeasonDivisionSubdivision": {
                  "data": {
                    "type": "leagueSeasonDivisionSubdivision",
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
  void noOneCanUpdateLeagueSeasonScore() throws Exception {
    mockMvc.perform(
      patch("/data/leagueSeasonScore/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeasonScore",
              "id": 1,
              "attributes": {
                "score": 15
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanDeleteLeagueSeasonScore() throws Exception {
    mockMvc.perform(
      delete("/data/leagueSeasonScore/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
