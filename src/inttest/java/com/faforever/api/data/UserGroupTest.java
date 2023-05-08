package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.Set;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
public class UserGroupTest extends AbstractIntegrationTest {
  private static final String testPatch = """
    {
      "data": {
          "type": "userGroup",
          "id": "3",
          "attributes": {
              "public": false
          },
          "relationships": {
              "members": {
                  "data": [{
                          "type": "player",
                          "id": "1"
                      }, {
                          "type": "player",
                          "id": "2"
                      }
                  ]
              },
              "permissions": {
                  "data": [{
                          "type": "groupPermission",
                          "id": "17"
                      }, {
                          "type": "groupPermission",
                          "id": "19"
                      }
                  ]
              }
          }
      }
    }
    """;

  private static final String testPost = """
    {
      "data": {
          "type": "userGroup",
          "attributes": {
              "technicalName": "faf_test",
              "nameKey": "faf.test",
              "public": false
          },
          "relationships": {
              "members": {
                  "data": []
              },
              "permissions": {
                  "data": [{
                          "type": "groupPermission",
                          "id": "17"
                      }, {
                          "type": "groupPermission",
                          "id": "19"
                      }
                  ]
              }
          }
      }
    }
    """;

  @Test
  public void canSeePublicGroupWithoutScope() throws Exception {
    mockMvc.perform(get("/data/userGroup/4")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk());
  }

  @Test
  public void cannotSeePrivateGroupWithoutScope() throws Exception {
    mockMvc.perform(get("/data/userGroup/5")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotSeePrivateGroupWithoutRole() throws Exception {
    mockMvc.perform(get("/data/userGroup/5")
      .with(getOAuthTokenWithActiveUser(OAuthScope._READ_SENSIBLE_USERDATA, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canSeePrivateGroupWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/userGroup/5")
      .with(getOAuthTokenWithActiveUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_READ_USER_GROUP)))
      .andExpect(status().isOk());
  }

  @Test
  public void cannotCreateUserGroupWithoutScope() throws Exception {
    mockMvc.perform(post("/data/userGroup")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_READ_USER_GROUP))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateUserGroupWithoutRole() throws Exception {
    mockMvc.perform(post("/data/userGroup")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateUserGroupWithScopeAndRole() throws Exception {
    mockMvc.perform(post("/data/userGroup")
        .with(getOAuthTokenWithActiveUser(
          Set.of(OAuthScope._ADMINISTRATIVE_ACTION, OAuthScope._READ_SENSIBLE_USERDATA),
          Set.of(GroupPermission.ROLE_WRITE_USER_GROUP, GroupPermission.ROLE_READ_USER_GROUP)
        ))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isCreated());
  }

  @Test
  public void cannotUpdateUserGroupWithoutScope() throws Exception {
    mockMvc.perform(patch("/data/userGroup/3")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_WRITE_USER_GROUP))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPatch))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateUserGroupWithoutRole() throws Exception {
    mockMvc.perform(patch("/data/userGroup/3")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPatch))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canUpdateUserGroupWithScopeAndRole() throws Exception {
    mockMvc.perform(patch("/data/userGroup/3")
      .with(getOAuthTokenWithActiveUser(
        Set.of(OAuthScope._ADMINISTRATIVE_ACTION, OAuthScope._READ_SENSIBLE_USERDATA),
        Set.of(GroupPermission.ROLE_WRITE_USER_GROUP, GroupPermission.ROLE_READ_USER_GROUP)
      ))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPatch))
      .andExpect(status().isNoContent());
  }
}
