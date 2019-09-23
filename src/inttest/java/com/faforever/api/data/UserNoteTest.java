package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.security.OAuthScope;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepUserNoteData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanUserNoteData.sql")
public class UserNoteTest extends AbstractIntegrationTest {
  /*
  {
      "data": {
          "type": "userNote",
          "attributes": {
              "watched": false,
              "note": "This note will be posted"
          },
          "relationships": {
              "author": {
                  "data": {
                      "type": "player",
                      "id": "1"
                  }
              },
              "player": {
                  "data": {
                      "type": "player",
                      "id": "3"
                  }
              }
          }
      }
  }
   */
  private static final String testPost = "{\"data\":{\"type\":\"userNote\",\"attributes\":{\"watched\":false,\"note\":\"This note will be posted\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"player\",\"id\":\"1\"}},\"player\":{\"data\":{\"type\":\"player\",\"id\":\"3\"}}}}}";
  @Autowired
  PlayerRepository playerRepository;

  @Test
  public void emptyResultWithoutScope() throws Exception {
    mockMvc.perform(get("/data/userNote")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_ACCOUNT_NOTE)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void emptyResultWithoutRole() throws Exception {
    mockMvc.perform(get("/data/userNote")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canReadUserNotesWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/userNote")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_ADMIN_ACCOUNT_NOTE)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  public void cannotReadSpecificUserNoteWithoutScope() throws Exception {
    mockMvc.perform(get("/data/userNote/1")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_ACCOUNT_NOTE)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotReadSpecificUserNoteWithoutRole() throws Exception {
    mockMvc.perform(get("/data/userNote/1")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canReadSpecificUserNoteWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/userNote/1")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_ADMIN_ACCOUNT_NOTE)))
      .andExpect(status().isOk());
  }

  @Test
  public void cannotCreateUserNoteWithoutScope() throws Exception {
    mockMvc.perform(post("/data/userNote")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_ACCOUNT_NOTE))
      .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateUserNoteWithoutRole() throws Exception {
    mockMvc.perform(post("/data/userNote")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, NO_AUTHORITIES))
      .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateUserNoteWithScopeAndRole() throws Exception {
    assertThat(playerRepository.getOne(3).getUserNotes().size(), is(0));

    mockMvc.perform(post("/data/userNote")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_ADMIN_ACCOUNT_NOTE))
      .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isCreated());

    assertThat(playerRepository.getOne(3).getUserNotes().size(), is(1));
  }
}
