package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapVersion.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanMapVersion.sql")
public class MapVersionElideTest extends AbstractIntegrationTest {

  private static final String MAP_VERSION_HIDE_FALSE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"hidden\": false\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MAP_VERSION_HIDE_TRUE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"hidden\": true\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MAP_VERSION_RANKED_FALSE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"ranked\": false\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MAP_VERSION_RANKED_TRUE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"ranked\": true\n" +
    "    }\n" +
    "  } \n" +
    "}";


  @Test
  public void canReadMapVersionWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/mapVersion")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  public void canUpdateMapVersionWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, GroupPermission.ROLE_ADMIN_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update hidden to false requires the extender permission, thus we test it here for the success case
        .content(MAP_VERSION_HIDE_FALSE_ID_1))
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotUpdateMapVersionWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update hidden to true is less restricted, thus we test it here for the failing case
        .content(MAP_VERSION_HIDE_TRUE_ID_1))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateMapVersionWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update hidden to true is less restricted, thus we test it here for the failing case
        .content(MAP_VERSION_HIDE_TRUE_ID_1))
      .andExpect(status().isForbidden());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void cannotUpdateHideToFalseAsEntityOwner() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(MAP_VERSION_HIDE_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void canUpdateRankedToFalseAsEntityOwner() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(MAP_VERSION_RANKED_FALSE_ID_1))
      .andExpect(status().isNoContent());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void cannotUpdateRankedToTrueAsEntityOwner() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(MAP_VERSION_RANKED_TRUE_ID_1))
      .andExpect(status().isForbidden());
  }
}
