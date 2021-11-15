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
class LeagueElideTest extends LeagueAbstractIntegrationTest {

  @Test
  void anyOneCanReadAllLeague() throws Exception {
    mockMvc.perform(
      get("/data/league")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[*]", hasSize(2)));
  }

  @Test
  void anyOneCanReadSpecificLeague() throws Exception {
    mockMvc.perform(
      get("/data/league/1")
    )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.technicalName", is("league1")))
      .andExpect(jsonPath("$.data.attributes.nameKey", is("league_name_1")))
      .andExpect(jsonPath("$.data.attributes.descriptionKey", is("league_description_1")));
  }

  @Test
  void noOneCanCreateLeague() throws Exception {
    mockMvc.perform(
      post("/data/league")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "league",
              "id": "100",
              "attributes": {
                "technicalName": "ForbiddenLeague",
                "nameKey": "ForbiddenLeague",
                "descriptionKey": "ForbiddenLeague"
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanUpdateLeague() throws Exception {
    mockMvc.perform(
      patch("/data/league/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content("""
          {
            "data": {
              "type": "league",
              "id": 1,
              "attributes": {
                "technicalName": "ForbiddenUpdateLeague"
              }
            }
          }
          """)
    )
      .andExpect(status().isForbidden());
  }

  @Test
  void noOneCanDeleteLeague() throws Exception {
    mockMvc.perform(
      delete("/data/league/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
