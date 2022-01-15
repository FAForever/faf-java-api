package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.mod.ModRepository;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModData.sql")
public class ModElideTest extends AbstractIntegrationTest {

  @Autowired
  ModRepository modRepository;

  private static final String MOD_RECOMMENDED_FALSE_ID_1 = """
    {
      "data": {
        "type": "mod",
        "id": "1",
        "attributes": {
          "recommended": false
        }
      }
    }""";

  private static final String MOD_RECOMMENDED_TRUE_ID_1 = """
    {
      "data": {
        "type": "mod",
        "id": "1",
        "attributes": {
          "recommended": true
        }
      }
    }""";


  @Test
  public void canReadModWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/mod")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(2)));
  }

  @Test
  public void canUpdateModRecommendationToTrueWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/mod/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._MANAGE_VAULT, GroupPermission.ROLE_ADMIN_MOD))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to true requires the vault permission, thus we test it here for the success case
        .content(MOD_RECOMMENDED_TRUE_ID_1))
      .andExpect(status().isNoContent());

    assertThat(modRepository.getById(1).getRecommended(), is(true));
  }

  @Test
  public void cannotUpdateModRecommendationToTrueWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/mod/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MOD))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to true requires the vault permission, thus we test it here for the failing case
        .content(MOD_RECOMMENDED_TRUE_ID_1))
      .andExpect(status().isForbidden());

    assertThat(modRepository.getById(1).getRecommended(), is(false));
  }

  @Test
  public void cannotUpdateModRecommendationToTrueWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/mod/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._MANAGE_VAULT, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to true requires the vault permission, thus we test it here for the failing case
        .content(MOD_RECOMMENDED_TRUE_ID_1))
      .andExpect(status().isForbidden());

    assertThat(modRepository.getById(1).getRecommended(), is(false));
  }

  @Test
  public void canUpdateModRecommendationToFalseWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/mod/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._MANAGE_VAULT, GroupPermission.ROLE_ADMIN_MOD))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to false requires the vault permission, thus we test it here for the success case
        .content(MOD_RECOMMENDED_FALSE_ID_1))
      .andExpect(status().isNoContent());

    assertThat(modRepository.getById(1).getRecommended(), is(false));
  }

  @Test
  public void cannotUpdateModRecommendationToFalseWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/mod/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MOD))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to false requires the vault permission, thus we test it here for the failing case
        .content(MOD_RECOMMENDED_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateModRecommendationToFalseWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/mod/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._MANAGE_VAULT, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to false requires the vault permission, thus we test it here for the failing case
        .content(MOD_RECOMMENDED_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }
}

