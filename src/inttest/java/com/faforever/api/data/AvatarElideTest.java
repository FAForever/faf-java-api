package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.avatar.AvatarAssignmentRepository;
import com.faforever.api.avatar.AvatarRepository;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.AvatarAssignment;
import com.faforever.api.data.domain.Player;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
  @Autowired
  AvatarRepository avatarRepository;
  @Autowired
  AvatarAssignmentRepository avatarAssignmentRepository;

  @Test
  public void getUnusedAvatar() throws Exception {
    Avatar avatar = avatarRepository.getOne(1);

    mockMvc.perform(get("/data/avatar/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is(String.valueOf(avatar.getId()))))
      .andExpect(jsonPath("$.data.type", is("avatar")))
      .andExpect(jsonPath("$.data.attributes.tooltip", is(avatar.getTooltip())))
      .andExpect(jsonPath("$.data.attributes.url", is(avatar.getUrl())))
      .andExpect(jsonPath("$.data.relationships.assignments.data", hasSize(0)));
  }

  @Test
  public void getAvatarWithPlayer() throws Exception {
    Avatar avatar = avatarRepository.getOne(2);

    mockMvc.perform(get("/data/avatar/2"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is(String.valueOf(avatar.getId()))))
      .andExpect(jsonPath("$.data.type", is("avatar")))
      .andExpect(jsonPath("$.data.attributes.tooltip", is(avatar.getTooltip())))
      .andExpect(jsonPath("$.data.attributes.url", is(avatar.getUrl())))
      .andExpect(jsonPath("$.data.relationships.assignments.data", hasSize(2)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanAssignAvatar() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Avatar.class, Player.class, AvatarAssignment.class);
    final Avatar avatar = new Avatar();
    avatar.setId(1);
    final Player player = new Player();
    player.setId(1);
    mockMvc.perform(
      post("/data/player/{playerId}/avatarAssignments", player.getId())
        .content(resourceConverter.writeDocument(new JSONAPIDocument<>(
            new AvatarAssignment()
              .setAvatar(avatar)
              .setSelected(null)
          )
        ))
    ).andExpect(status().isCreated());
    Optional<AvatarAssignment> createdAssignment = avatarAssignmentRepository.findOneByAvatarIdAndPlayerId(1, 1);
    assertThat(createdAssignment.isPresent(), is(true));
    assertThat(createdAssignment.get().getPlayer().getId(), is(player.getId()));
    assertThat(createdAssignment.get().getAvatar().getId(), is(avatar.getId()));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanDeleteAvatarAssignment() throws Exception {
    mockMvc.perform(
      delete("/data/avatarAssignment/{assignmentId}", 1)
    ).andExpect(status().isNoContent());
    final Optional<AvatarAssignment> deletedAssignment = avatarAssignmentRepository.findOneById(1);
    assertThat(deletedAssignment.isPresent(), is(false));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotAssignAvatar() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Avatar.class, Player.class, AvatarAssignment.class);
    final Avatar avatar = new Avatar();
    avatar.setId(1);
    final Player player = new Player();
    player.setId(1);
    mockMvc.perform(
      post("/data/player/{playerId}/avatarAssignments", player.getId())
        .content(resourceConverter.writeDocument(new JSONAPIDocument<>(
            new AvatarAssignment()
              .setAvatar(avatar)
              .setSelected(null)
          )
        ))
    ).andExpect(status().isForbidden());
    final Optional<AvatarAssignment> createdAssignment = avatarAssignmentRepository.findOneByAvatarAndPlayer(avatar, player);
    assertThat(createdAssignment.isPresent(), is(false));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotDeleteAvatarAssignment() throws Exception {
    mockMvc.perform(
      delete("/data/avatarAssignment/{assignmentId}", 1)
    ).andExpect(status().isForbidden());
    final Optional<AvatarAssignment> stillExistingAssignment = avatarAssignmentRepository.findOneById(1);
    assertThat(stillExistingAssignment.isPresent(), is(true));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanUpdateAvatarAssignmentExpiration() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Avatar.class, Player.class, AvatarAssignment.class);
    final OffsetDateTime now = OffsetDateTime.now();
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .content(resourceConverter.writeDocument(new JSONAPIDocument<>(
            new AvatarAssignment()
              .setSelected(null)
              .setExpiresAt(now)
              .setId(1)
          )
        ))
    ).andExpect(status().isNoContent());
    final Optional<AvatarAssignment> updatedAssignment = avatarAssignmentRepository.findOneById(1);
    assertThat(updatedAssignment.isPresent(), is(true));
    assertThat(updatedAssignment.get().getExpiresAt(), is(now));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotUpdateAvatarAssignmentExpiration() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Avatar.class, Player.class, AvatarAssignment.class);
    final OffsetDateTime now = OffsetDateTime.now();
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .content(resourceConverter.writeDocument(new JSONAPIDocument<>(
            new AvatarAssignment()
              .setSelected(null)
              .setExpiresAt(now)
              .setId(1)
          )
        ))
    ).andExpect(status().isForbidden());
    final Optional<AvatarAssignment> updatedAssignment = avatarAssignmentRepository.findOneById(1);
    assertThat(updatedAssignment.isPresent(), is(true));
    assertThat(updatedAssignment.get().getExpiresAt(), is(nullValue()));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void ownerCanUpdateAvatarAssignmentSelection() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Avatar.class, Player.class, AvatarAssignment.class);
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .content(resourceConverter.writeDocument(new JSONAPIDocument<>(
            new AvatarAssignment()
              .setSelected(false)
              .setId(1)
          )
        ))
    ).andExpect(status().isNoContent());
    final Optional<AvatarAssignment> updatedAssignment = avatarAssignmentRepository.findOneById(1);
    assertThat(updatedAssignment.isPresent(), is(true));
    assertThat(updatedAssignment.get().isSelected(), is(false));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void nonOwnerCannotUpdateAvatarAssignmentSelection() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Avatar.class, Player.class, AvatarAssignment.class);
    mockMvc.perform(
      patch("/data/avatarAssignment/{assignmentId}", 1)
        .content(resourceConverter.writeDocument(new JSONAPIDocument<>(
            new AvatarAssignment()
              .setSelected(false)
              .setId(1)
          )
        ))
    ).andExpect(status().isForbidden());
    final Optional<AvatarAssignment> updatedAssignment = avatarAssignmentRepository.findOneById(1);
    assertThat(updatedAssignment.isPresent(), is(true));
    assertThat(updatedAssignment.get().isSelected(), is(true));
  }
}
