package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.faforever.commons.api.dto.Avatar;
import com.faforever.commons.api.dto.AvatarAssignment;
import com.faforever.commons.api.dto.Player;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepAvatarData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanAvatarData.sql")
public class AvatarAssignmentElideTest extends AbstractIntegrationTest {

  @Test
  public void getUnusedAvatar() throws Exception {
    mockMvc.perform(get("/data/avatar/3"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is("3")))
      .andExpect(jsonPath("$.data.type", is("avatar")))
      .andExpect(jsonPath("$.data.attributes.tooltip", is("Donator Avatar")))
      .andExpect(jsonPath("$.data.attributes.url", is("http://localhost/faf/avatars/donator.png")))
      .andExpect(jsonPath("$.data.relationships.assignments.data", hasSize(0)));
  }

  @Test
  public void getAvatarWithPlayer() throws Exception {
    mockMvc.perform(get("/data/avatar/2"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is("2")))
      .andExpect(jsonPath("$.data.type", is("avatar")))
      .andExpect(jsonPath("$.data.attributes.tooltip", is("Avatar No. 2")))
      .andExpect(jsonPath("$.data.attributes.url", is("http://localhost/faf/avatars/avatar2.png")))
      .andExpect(jsonPath("$.data.relationships.assignments.data", hasSize(1)));
  }

  @Test
  public void canAssignAvatarWithScopeAndRole() throws Exception {
    final Avatar avatar = (Avatar) new Avatar().setId("1");
    final Player player = (Player) new Player().setId("1");
    final AvatarAssignment avatarAssignment = new AvatarAssignment()
      .setAvatar(avatar)
      .setSelected(false);
    mockMvc.perform(
      post("/data/player/{playerId}/avatarAssignments", player.getId())
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_AVATAR))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.relationships.player.data.id", is(player.getId())))
      .andExpect(jsonPath("$.data.relationships.avatar.data.id", is(avatar.getId())));
  }

  @Test
  public void cannotAssignAvatarWithoutScope() throws Exception {
    final Avatar avatar = (Avatar) new Avatar().setId("1");
    final Player player = (Player) new Player().setId("1");
    final AvatarAssignment avatarAssignment = new AvatarAssignment()
      .setAvatar(avatar)
      .setSelected(false);
    mockMvc.perform(
      post("/data/player/{playerId}/avatarAssignments", player.getId())
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_AVATAR))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotAssignAvatarWithoutRole() throws Exception {
    final Avatar avatar = (Avatar) new Avatar().setId("1");
    final Player player = (Player) new Player().setId("1");
    final AvatarAssignment avatarAssignment = new AvatarAssignment()
      .setAvatar(avatar)
      .setSelected(false);
    mockMvc.perform(
      post("/data/player/{playerId}/avatarAssignments", player.getId())
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canDeleteAvatarAssignmentWithScopeAndRole() throws Exception {
    mockMvc.perform(
      delete("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isNotFound());
  }

  @Test
  public void cannotDeleteAvatarAssignmentWithoutScope() throws Exception {
    mockMvc.perform(
      delete("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isForbidden());
  }

  @Test
  public void cannotDeleteAvatarAssignmentWithoutRole() throws Exception {
    mockMvc.perform(
      delete("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
    ).andExpect(status().isForbidden());
  }

  @Test
  public void canUpdateAvatarAssignmentExpirationWithScopeAndRole() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setExpiresAt(now)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_AVATAR))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/avatarAssignment/{assignmentId}", 1))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.expiresAt", is(OFFSET_DATE_TIME_FORMATTER.format(now))));
  }

  @Test
  public void cannotUpdateAvatarAssignmentExpirationWithoutScope() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setExpiresAt(now)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_AVATAR))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateAvatarAssignmentExpirationWithoutRole() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setExpiresAt(now)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void ownerCanUpdateAvatarAssignmentSelection() throws Exception {
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setSelected(true)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/avatarAssignment/{assignmentId}", 1)
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.selected", is(true)));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void nonOwnerCannotUpdateAvatarAssignmentSelection() throws Exception {
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setSelected(false)
      .setId("2");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 2)
        .with(getOAuthTokenWithoutUser(OAuthScope._PUBLIC_PROFILE))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isForbidden());
  }
}
