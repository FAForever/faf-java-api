package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.avatar.AvatarRepository;
import com.faforever.api.data.domain.Avatar;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepAvatarData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanAvatarData.sql")
public class AvatarElideTest extends AbstractIntegrationTest {
  @Autowired
  AvatarRepository avatarRepository;

  @Test
  public void getUnusedAvatar() throws Exception {
    Avatar avatar = avatarRepository.findOne(1);

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
    Avatar avatar = avatarRepository.findOne(2);

    mockMvc.perform(get("/data/avatar/2"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is(String.valueOf(avatar.getId()))))
      .andExpect(jsonPath("$.data.type", is("avatar")))
      .andExpect(jsonPath("$.data.attributes.tooltip", is(avatar.getTooltip())))
      .andExpect(jsonPath("$.data.attributes.url", is(avatar.getUrl())))
      .andExpect(jsonPath("$.data.relationships.assignments.data", hasSize(2)));
  }
}
