package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.map.MapRepository;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapVersion.sql")
public class MapElideTest extends AbstractIntegrationTest {

  @Autowired
  MapRepository mapRepository;

  private static final String MAP_RECOMMENDED_FALSE_ID_1 = """
    {
      "data": {
        "type": "map",
        "id": "1",
        "attributes": {
          "recommended": false
        }
      }
    }""";

  private static final String MAP_RECOMMENDED_TRUE_ID_1 = """
    {
      "data": {
        "type": "map",
        "id": "1",
        "attributes": {
          "recommended": true
        }
      }
    }""";


  @Test
  public void canReadMapWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/map")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  public void canUpdateMapRecommendationToTrueWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/map/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, GroupPermission.ROLE_ADMIN_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to true requires the vault permission, thus we test it here for the success case
        .content(MAP_RECOMMENDED_TRUE_ID_1))
      .andExpect(status().isNoContent());

    assertThat(mapRepository.getOne(1).getRecommended(), is(true));
  }

  @Test
  public void cannotUpdateMapRecommendationToTrueWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/map/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to true requires the vault permission, thus we test it here for the failing case
        .content(MAP_RECOMMENDED_TRUE_ID_1))
      .andExpect(status().isForbidden());

    assertThat(mapRepository.getOne(1).getRecommended(), is(false));
  }

  @Test
  public void cannotUpdateMapRecommendationToTrueWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/map/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to true requires the vault permission, thus we test it here for the failing case
        .content(MAP_RECOMMENDED_TRUE_ID_1))
      .andExpect(status().isForbidden());

    assertThat(mapRepository.getOne(1).getRecommended(), is(false));
  }

  @Test
  public void canUpdateMapRecommendationToFalseWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/map/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, GroupPermission.ROLE_ADMIN_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to false requires the vault permission, thus we test it here for the success case
        .content(MAP_RECOMMENDED_FALSE_ID_1))
      .andExpect(status().isNoContent());

    assertThat(mapRepository.getOne(1).getRecommended(), is(false));
  }

  @Test
  public void cannotUpdateMapRecommendationToFalseWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/map/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to false requires the vault permission, thus we test it here for the failing case
        .content(MAP_RECOMMENDED_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateMapRecommendationToFalseWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/map/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._MANAGE_VAULT, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        // update recommended to false requires the vault permission, thus we test it here for the failing case
        .content(MAP_RECOMMENDED_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }
}

