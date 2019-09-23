package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModData.sql")
public class ModVersionElideTest extends AbstractIntegrationTest {

  private static final String MOD_VERSION_HIDE_FALSE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"modVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"hidden\": false\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MOD_VERSION_HIDE_TRUE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"modVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"hidden\": true\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MOD_VERSION_RANKED_FALSE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"modVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"ranked\": false\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MOD_VERSION_RANKED_TRUE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"modVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"ranked\": true\n" +
    "    }\n" +
    "  } \n" +
    "}";


  @Test
  public void canReadModVersionWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/modVersion")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(2)));
  }

  @Test
  public void canUpdateModVersionWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/modVersion/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, GroupPermission.ROLE_ADMIN_MOD))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(MOD_VERSION_HIDE_FALSE_ID_1))
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotModVersionBanWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/modVersion/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MOD))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(MOD_VERSION_HIDE_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotModVersionBanWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/modVersion/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(MOD_VERSION_HIDE_TRUE_ID_1))
      .andExpect(status().isForbidden());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void cannotUpdateHideToFalseAsEntityOwner() throws Exception {
    mockMvc.perform(
      patch("/data/modVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(MOD_VERSION_HIDE_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void cannotUpdateRankedToFalseAsEntityOwner() throws Exception {
    mockMvc.perform(
      patch("/data/modVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(MOD_VERSION_RANKED_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void cannotUpdateRankedToTrueAsEntityOwner() throws Exception {
    mockMvc.perform(
      patch("/data/modVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(MOD_VERSION_RANKED_TRUE_ID_1))
      .andExpect(status().isForbidden());
  }
}
