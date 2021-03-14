package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
public class MapPoolElideTest extends AbstractIntegrationTest {
  private static final String NEW_LADDER_MAP_BODY = "{\"data\":{\"type\":\"mapPoolAssignment\",\"relationships\":{\"mapVersion\":{\"data\":{\"type\":\"mapVersion\",\"id\":\"2\"}}}}}";

  @Test
  public void cannotCreateMapPoolItemWithoutScope() throws Exception {
    mockMvc.perform(
      post("/data/mapPool/1/relationships/mapPoolAssignments")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateMapPoolItemWithoutRole() throws Exception {
    mockMvc.perform(
      post("/data/mapPool/1/relationships/mapPoolAssignments")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateMapPoolItemWithScopeAndRole() throws Exception {
    mockMvc.perform(
      post("/data/mapPool/1/relationships/mapPoolAssignments")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isNoContent());
  }

  @Test
  public void canDeleteMapPoolItemWithScopeAndRole() throws Exception {
    mockMvc.perform(
      delete("/data/mapPool/1/relationships/mapPoolAssignments/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotDeleteMapPoolItemWithoutScope() throws Exception {
    mockMvc.perform(
      delete("/data/mapPool/1/relationships/mapPoolAssignments/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotDeleteMapPoolItemWithoutRole() throws Exception {
    mockMvc.perform(
      delete("/data/mapPool/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }
}
