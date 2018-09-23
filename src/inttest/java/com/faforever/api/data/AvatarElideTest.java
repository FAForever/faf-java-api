package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
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
public class AvatarElideTest extends AbstractIntegrationTest {


  @Test
  public void getUnusedAvatar() throws Exception {
    mockMvc.perform(get("/data/avatar/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is("1")))
      .andExpect(jsonPath("$.data.type", is("avatar")))
      .andExpect(jsonPath("$.data.attributes.tooltip", is("Avatar No. 1")))
      .andExpect(jsonPath("$.data.attributes.url", is("http://localhost/faf/avatars/avatar1.png")))
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
      .andExpect(jsonPath("$.data.relationships.assignments.data", hasSize(2)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanAssignAvatar() throws Exception {
    final Avatar avatar = (Avatar) new Avatar().setId("1");
    final Player player = (Player) new Player().setId("1");
    final AvatarAssignment avatarAssignment = new AvatarAssignment()
      .setAvatar(avatar)
      .setSelected(false);
    mockMvc.perform(
      post("/data/player/{playerId}/avatarAssignments", player.getId())
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.relationships.player.data.id", is(player.getId())))
      .andExpect(jsonPath("$.data.relationships.avatar.data.id", is(avatar.getId())));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanDeleteAvatarAssignment() throws Exception {
    mockMvc.perform(
      delete("/data/avatarAssignment/{assignmentId}", 1)
    ).andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/avatarAssignment/{assignmentId}", 1)
    ).andExpect(status().isNotFound());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotAssignAvatar() throws Exception {
    final Avatar avatar = (Avatar) new Avatar().setId("1");
    final Player player = (Player) new Player().setId("1");
    final AvatarAssignment avatarAssignment = new AvatarAssignment()
      .setAvatar(avatar)
      .setSelected(false);
    mockMvc.perform(
      post("/data/player/{playerId}/avatarAssignments", player.getId())
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment))
    ).andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotDeleteAvatarAssignment() throws Exception {
    mockMvc.perform(
      delete("/data/avatarAssignment/{assignmentId}", 1)
    ).andExpect(status().isForbidden());
    mockMvc.perform(
      get("/data/avatarAssignment/{assignmentId}", 1)
    ).andExpect(status().isOk());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanUpdateAvatarAssignmentExpiration() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setExpiresAt(now)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/avatarAssignment/{assignmentId}", 1))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.expiresAt", is(OFFSET_DATE_TIME_FORMATTER.format(now))));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotUpdateAvatarAssignmentExpiration() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setExpiresAt(now)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void ownerCanUpdateAvatarAssignmentSelection() throws Exception {
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setSelected(true)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/avatarAssignment/{assignmentId}", 1))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.selected", is(true)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void nonOwnerCannotUpdateAvatarAssignmentSelection() throws Exception {
    final AvatarAssignment avatarAssignment = (AvatarAssignment) new AvatarAssignment()
      .setSelected(false)
      .setId("1");
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(avatarAssignment)))
      .andExpect(status().isForbidden());
  }
}
