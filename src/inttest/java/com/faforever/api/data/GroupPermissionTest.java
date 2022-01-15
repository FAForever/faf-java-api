package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
public class GroupPermissionTest extends AbstractIntegrationTest {
  private static final String testPost = """
  {
      "data": {
          "type": "groupPermission",
          "attributes": {
              "technicalName": "test",
              "nameKey": "test"
          }
      }
  }
  """;

  @Test
  public void emptyResultWithoutScope() throws Exception {
    mockMvc.perform(get("/data/groupPermission")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_READ_USER_GROUP)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void emptyResultWithoutRole() throws Exception {
    mockMvc.perform(get("/data/groupPermission")
      .with(getOAuthTokenWithActiveUser(OAuthScope._READ_SENSIBLE_USERDATA, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canReadPermissionsWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/groupPermission")
      .with(getOAuthTokenWithActiveUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_READ_USER_GROUP)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(25)));
  }

  @Test
  public void cannotCreatePermissionWithoutScope() throws Exception {
    mockMvc.perform(post("/data/groupPermission")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_WRITE_USER_GROUP))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreatePermissionWithoutRole() throws Exception {
    mockMvc.perform(post("/data/groupPermission")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreatePermissionWithScopeAndRole() throws Exception {
    mockMvc.perform(post("/data/groupPermission")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_USER_GROUP))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }
}
