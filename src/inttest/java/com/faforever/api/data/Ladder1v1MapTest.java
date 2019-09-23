package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanMapData.sql")
public class Ladder1v1MapTest extends AbstractIntegrationTest {
  private static final String NEW_LADDER_MAP_BODY = "{\"data\":{\"type\":\"ladder1v1Map\",\"relationships\":{\"mapVersion\":{\"data\":{\"type\":\"mapVersion\",\"id\":\"2\"}}}}}";

  @Test
  public void cannotCreateLadderMapWithoutScope() throws Exception {
    mockMvc.perform(
      post("/data/ladder1v1Map")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateLadderMapWithoutRole() throws Exception {
    mockMvc.perform(
      post("/data/ladder1v1Map")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateLadderMapWithScopeAndRole() throws Exception {
    mockMvc.perform(
      post("/data/ladder1v1Map")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isCreated());
  }

  @Test
  public void canDeleteLadderMapWithScopeAndRole() throws Exception {
    mockMvc.perform(
      delete("/data/ladder1v1Map/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))) // magic value from prepMapData.sql
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotDeleteLadderMapWithoutScope() throws Exception {
    mockMvc.perform(
      delete("/data/ladder1v1Map/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotDeleteLadderMapWithoutRole() throws Exception {
    mockMvc.perform(
      delete("/data/ladder1v1Map/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }
}
