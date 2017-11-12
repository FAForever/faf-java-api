package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepTeamkillData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanTeamkillData.sql")
public class TeamkillTest extends AbstractIntegrationTest {
  @Test
  @WithUserDetails(AUTH_USER)
  public void emptyResultTeamkillsAsUser() throws Exception {
    mockMvc.perform(get("/data/teamkill"))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[]}"));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotReadSpecificTeamkillAsUser() throws Exception {
    mockMvc.perform(get("/data/teamkill/1"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadTeamkillsAsModerator() throws Exception {
    mockMvc.perform(get("/data/teamkill"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadSpecificTeamkillAsModerator() throws Exception {
    mockMvc.perform(get("/data/teamkill/1"))
      .andExpect(status().isOk());
  }
}
