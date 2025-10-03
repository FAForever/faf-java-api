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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMatchmakerQueueMapPoolData.sql")
public class MatchmakerQueueMapPoolElideTest extends AbstractIntegrationTest {

  private static <T> String genUpdateRequestForField(String fieldName, T fieldValue) {
    if (fieldValue instanceof String) {
      fieldValue = (T) ("\"" + fieldValue + "\"");
    }

    return """
      {
        "data": {
          "type": "matchmakerQueueMapPool",
          "id": "101",
          "attributes": {
            "%s": %s
          }
        }
      }
      """.formatted(fieldName, fieldValue);
  }

  @Test
  public void canReadMatchmakerQueueMapPoolWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/matchmakerQueueMapPool")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(5)));
  }

  @Test
  public void canReadMatchmakerQueueMapPoolParamsWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is("101")))
      .andExpect(jsonPath("$.data.type", is("matchmakerQueueMapPool")))
      .andExpect(jsonPath("$.data.attributes.maxRating", is(800.0)))
      .andExpect(jsonPath("$.data.attributes.minRating", is(300.0)))
      .andExpect(jsonPath("$.data.attributes.vetoTokensPerPlayer", is(1)))
      .andExpect(jsonPath("$.data.attributes.minimumMapsAfterVeto", is(1.5)))
      .andExpect(jsonPath("$.data.attributes.maxTokensPerMap", is(1)))
      .andExpect(jsonPath("$.data.relationships.mapPool.data.id", is("2")))
      .andExpect(jsonPath("$.data.relationships.matchmakerQueue.data.id", is("1")));
  }

  @Test
  public void cannotUpdateMatchmakerQueueMapPoolMinRatingWithoutScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("minRating", 1000)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateMatchmakerQueueMapPoolMaxRatingWithoutScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("maxRating", 1000)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateMatchmakerQueueMapPoolVetoTokensPerPlayerWithoutScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("vetoTokensPerPlayer", 1)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateMatchmakerQueueMapPoolMaxTokensPerMapWithoutScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("maxTokensPerMap", 2)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateMatchmakerQueueMapPoolMinimumMapsAfterVetoWithoutScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("minimumMapsAfterVeto", 3.1)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canUpdateMatchmakerQueueMapPoolMinRatingWithScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("minRating", 1000)))
      .andExpect(status().isNoContent());
  }

  @Test
  public void canUpdateMatchmakerQueueMapPoolMaxRatingWithScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("maxRating", 1000)))
      .andExpect(status().isNoContent());
  }

  @Test
  public void canUpdateMatchmakerQueueMapPoolVetoTokensPerPlayerWithScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("vetoTokensPerPlayer", 1)))
      .andExpect(status().isNoContent());
  }

  @Test
  public void canUpdateMatchmakerQueueMapPoolMaxTokensPerMapWithScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("maxTokensPerMap", 2)))
      .andExpect(status().isNoContent());
  }

  @Test
  public void canUpdateMatchmakerQueueMapPoolMinimumMapsAfterVetoWithScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/matchmakerQueueMapPool/101")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_MATCHMAKER_MAP))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(genUpdateRequestForField("minimumMapsAfterVeto", 3.1)))
      .andExpect(status().isNoContent());
  }
}
