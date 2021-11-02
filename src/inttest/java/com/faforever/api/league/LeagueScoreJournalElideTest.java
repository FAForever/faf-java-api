package com.faforever.api.league;

import com.faforever.api.config.LeagueDatasourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/truncateTables.sql", config = @SqlConfig(dataSource = LeagueDatasourceConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatasourceConfig.LEAGUE_TRANSACTION_MANAGER))
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/prepLeagueData.sql", config = @SqlConfig(dataSource = LeagueDatasourceConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatasourceConfig.LEAGUE_TRANSACTION_MANAGER))
public class LeagueScoreJournalElideTest extends LeagueAbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        get("/data/leagueScoreJournal")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(11)));
  }

  @Test
  void anyOneCanReadPlayerAndSeasonLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        get("/data/leagueScoreJournal?filter=loginId==1;leagueSeason.id==1")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(1)));
  }

  @Test
  void anyOneCanReadSpecificLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        get("/data/leagueScoreJournal/1")
      )
      .andExpect(status().isOk());
  }

  @Test
  void noOneCanCreateLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        post("/data/leagueScoreJournal")
          .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
          .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
          .content("""
          {
            "data": {
              "type": "leagueScoreJournal",
              "attributes": {
                "gameCount": 10,
                "loginId": 10,
                "scoreBefore": 10,
                "scoreAfter": 10
              },
              "relationships": {
                "leagueSeason": {
                  "data": {
                    "type": "leagueSeason",
                    "id": "1"
                  }
                },
                "leagueSeasonDivisionSubdivisionBefore": {
                  "data": {
                    "type": "leagueSeasonDivisionSubdivision",
                    "id": "1"
                  }
                }
                "leagueSeasonDivisionSubdivisionAfter": {
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
  void noOneCanUpdateLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        patch("/data/leagueScoreJournal/1")
          .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
          .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
          .content("""
          {
            "data": {
              "type": "leagueScoreJournal",
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
  void noOneCanDeleteLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        delete("/data/leagueScoreJournal/1")
          .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
