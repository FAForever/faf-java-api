package com.faforever.api.league;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.config.LeagueDatastoreConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
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

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/truncateTables.sql", config = @SqlConfig(dataSource = LeagueDatastoreConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatastoreConfig.LEAGUE_TRANSACTION_MANAGER))
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/league/prepLeagueData.sql", config = @SqlConfig(dataSource = LeagueDatastoreConfig.LEAGUE_DATA_SOURCE, transactionManager = LeagueDatastoreConfig.LEAGUE_TRANSACTION_MANAGER))
public class LeagueScoreJournalElideTest extends AbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        get("/data/leagueScoreJournal")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(4)));
  }

  @Test
  void anyOneCanReadPlayerAndSeasonLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        get("/data/leagueScoreJournal?filter=loginId==1;leagueSeason.id==1")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(2)));
  }

  @Test
  void anyOneCanReadSpecificLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        get("/data/leagueScoreJournal/1")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.gameId", is(2)))
      .andExpect(jsonPath("$.data.attributes.loginId", is(1)))
      .andExpect(jsonPath("$.data.relationships.leagueSeason.data.id", is(1)))
      .andExpect(jsonPath("$.data.relationships.leagueSeasonDivisionSubdivisionBefore.data.id", is(1)))
      .andExpect(jsonPath("$.data.relationships.leagueSeasonDivisionSubdivisionAfter.data.id", is(2)))
      .andExpect(jsonPath("$.data.attributes.scoreBefore", is(10)))
      .andExpect(jsonPath("$.data.attributes.scoreAfter", is(2)))
      .andExpect(jsonPath("$.data.attributes.gameCount", is(22)));
  }

  @Test
  void noOneCanCreateLeagueScoreJournal() throws Exception {
    mockMvc.perform(
        post("/data/leagueScoreJournal")
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
          .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
          .content("""
          {
            "data": {
              "type": "leagueScoreJournal",
              "attributes": {
                "gameId": 10,
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
                },
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
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
          .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
          .content("""
          {
            "data": {
              "type": "leagueScoreJournal",
              "id": 1,
              "attributes": {
                "scoreAfter": 15
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
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
