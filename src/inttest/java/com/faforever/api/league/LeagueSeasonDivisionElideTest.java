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
class LeagueSeasonDivisionElideTest extends LeagueAbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeagueSeasonDivision() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeasonDivision")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(4)));
  }

  @Test
  void anyOneCanReadSpecificLeagueSeasonDivision() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeasonDivision/1")
    )
      .andExpect(status().isOk());
  }

  @Test
  void noOneCanCreateLeagueSeasonDivision() throws Exception {
    mockMvc.perform(
      post("/data/leagueSeasonDivision")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeasonDivision",
              "id": "1",
              "attributes": {
                "descriptionKey": "division_description_1",
                "divisionIndex": 1,
                "nameKey": "division_name_1"
              },
              "relationships": {
                "leagueSeason": {
                  "data": {
                    "type": "leagueSeason",
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
  void noOneCanUpdateLeagueSeasonDivision() throws Exception {
    mockMvc.perform(
      patch("/data/leagueSeasonDivision/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeasonDivision",
              "id": 1,
              "attributes": {
                "divisionIndex": 2
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanDeleteLeagueSeasonDivision() throws Exception {
    mockMvc.perform(
      delete("/data/leagueSeasonDivision/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
