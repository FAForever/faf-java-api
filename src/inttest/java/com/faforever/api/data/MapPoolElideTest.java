package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
public class MapPoolElideTest extends AbstractIntegrationTest {
  private static final String NEW_LADDER_MAP_BODY = """
    {
    "data": {
      "type": "mapPoolAssignment",
      "attributes": {
        "weight": 1,
        "mapParams": {"type":"neroxis","size":512,"spawns":2,"version":"0.0.0"}
      },
      "relationships": {
        "mapPool": {
          "data": {
            "type": "mapPool",
            "id": "1"
          }
        },
        "mapVersion": {
          "data": {
            "type": "mapVersion",
            "id": "1"
          }
        }
      }
    }
    }""";


  @Test
  public void getMapPoolAssignmentWithMapVersion() throws Exception {
    mockMvc.perform(get("/data/mapPoolAssignment/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is("1")))
      .andExpect(jsonPath("$.data.type", is("mapPoolAssignment")))
      .andExpect(jsonPath("$.data.attributes.weight", is(1)))
      .andExpect(jsonPath("$.data.attributes.mapParams", nullValue()))
      .andExpect(jsonPath("$.data.relationships.mapPool.data.id", is("1")))
      .andExpect(jsonPath("$.data.relationships.mapVersion.data.id", is("1")));
  }

  @Test
  public void getMapPoolAssignmentWithMapParams() throws Exception {
    mockMvc.perform(get("/data/mapPoolAssignment/2")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is("2")))
      .andExpect(jsonPath("$.data.type", is("mapPoolAssignment")))
      .andExpect(jsonPath("$.data.attributes.weight", is(1)))
      .andExpect(jsonPath("$.data.attributes.mapParams.type", is("neroxis")))
      .andExpect(jsonPath("$.data.attributes.mapParams.version", is("1.4.3")))
      .andExpect(jsonPath("$.data.attributes.mapParams.size", is(512)))
      .andExpect(jsonPath("$.data.attributes.mapParams.spawns", is(2)))
      .andExpect(jsonPath("$.data.relationships.mapPool.data.id", is("1")))
      .andExpect(jsonPath("$.data.relationships.mapVersion.data", is(nullValue())));
  }

  @Test
  public void cannotCreateMapPoolItemWithoutScope() throws Exception {
    mockMvc.perform(
      post("/data/mapPoolAssignment")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateMapPoolItemWithoutRole() throws Exception {
    mockMvc.perform(
      post("/data/mapPoolAssignment")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateMapPoolItemWithScopeAndRole() throws Exception {
    mockMvc.perform(
      post("/data/mapPoolAssignment")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.type", is("mapPoolAssignment")))
      .andExpect(jsonPath("$.data.attributes.weight", is(1)))
      .andExpect(jsonPath("$.data.attributes.mapParams.type", is("neroxis")))
      .andExpect(jsonPath("$.data.attributes.mapParams.version", is("0.0.0")))
      .andExpect(jsonPath("$.data.attributes.mapParams.size", is(512)))
      .andExpect(jsonPath("$.data.attributes.mapParams.spawns", is(2)))
      .andExpect(jsonPath("$.data.relationships.mapPool.data.id", is("1")))
      .andExpect(jsonPath("$.data.relationships.mapVersion.data.id", is("1")));;
  }

  @Test
  public void canDeleteMapPoolItemWithScopeAndRole() throws Exception {
    mockMvc.perform(
      delete("/data/mapPoolAssignment/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP)))
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotDeleteMapPoolItemWithoutScope() throws Exception {
    mockMvc.perform(
      delete("/data/mapPoolAssignment/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotDeleteMapPoolItemWithoutRole() throws Exception {
    mockMvc.perform(
      delete("/data/mapPoolAssignment/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }
}
