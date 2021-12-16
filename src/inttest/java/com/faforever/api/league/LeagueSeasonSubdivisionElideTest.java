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
class LeagueSeasonSubdivisionElideTest extends LeagueAbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeagueSeasonSubdivision() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeasonDivisionSubdivision")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(4)));
  }

  @Test
  void anyOneCanReadSpecificLeagueSeasonSubdivision() throws Exception {
    mockMvc.perform(
      get("/data/leagueSeasonDivisionSubdivision/1")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.subdivisionIndex", is(1)))
      .andExpect(jsonPath("$.data.attributes.nameKey", is("subdivision_name_1")))
      .andExpect(jsonPath("$.data.attributes.descriptionKey", is("subdivision_description_1")))
      .andExpect(jsonPath("$.data.attributes.minRating", is(0.0)))
      .andExpect(jsonPath("$.data.attributes.maxRating", is(1000.0)))
      .andExpect(jsonPath("$.data.attributes.highestScore", is(50)))
      .andExpect(jsonPath("$.data.attributes.imageUrl", is("https://example1.com/division_name_1subdivision_name_1.png")))
      .andExpect(jsonPath("$.data.attributes.mediumImageUrl", is("https://example1.com/medium/division_name_1subdivision_name_1_medium.png")))
      .andExpect(jsonPath("$.data.attributes.smallImageUrl", is("https://example1.com/small/division_name_1subdivision_name_1_small.png")));
  }

  @Test
  void noOneCanCreateLeagueSeasonSubdivision() throws Exception {
    mockMvc.perform(
      post("/data/leagueSeasonDivisionSubdivision")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeasonDivisionSubdivision",
              "attributes": {
               "descriptionKey": "subdivision_description_1",
               "highestScore": 50,
               "maxRating": 1000.0,
               "minRating": 0.0,
               "nameKey": "subdivision_name_1",
               "subdivisionIndex": 1
              },
              "relationships": {
                "leagueSeasonDivision": {
                  "data": {
                    "type": "leagueSeasonDivision",
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
  void noOneCanUpdateLeagueSeasonSubdivision() throws Exception {
    mockMvc.perform(
      patch("/data/leagueSeasonDivisionSubdivision/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "leagueSeasonDivisionSubdivision",
              "id": 1,
              "attributes": {
                "maxRating": 1500.0
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanDeleteLeagueSeasonSubdivision() throws Exception {
    mockMvc.perform(
      delete("/data/leagueSeasonDivisionSubdivision/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
