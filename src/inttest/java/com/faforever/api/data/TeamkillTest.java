package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepTeamkillData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanTeamkillData.sql")
public class TeamkillTest extends AbstractIntegrationTest {

  @Test
  public void emptyResultTeamkillsWithoutScope() throws Exception {
    mockMvc.perform(get("/data/teamkill")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_READ_TEAMKILL_REPORT)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void emptyResultTeamkillsWithoutRole() throws Exception {
    mockMvc.perform(get("/data/teamkill")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canReadTeamkillsWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/teamkill")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_READ_TEAMKILL_REPORT)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  public void cannotReadSpecificTeamkillWithoutScope() throws Exception {
    mockMvc.perform(get("/data/teamkill/1")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_READ_TEAMKILL_REPORT)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotReadSpecificTeamkillWithoutRole() throws Exception {
    mockMvc.perform(get("/data/teamkill/1")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canReadSpecificTeamkillWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/teamkill/1")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_READ_TEAMKILL_REPORT)))
      .andExpect(status().isOk());
  }
}
