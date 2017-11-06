package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanMapData.sql")
public class Ladder1v1MapTest extends AbstractIntegrationTest {
  private static final String NEW_LADDER_MAP_BODY = "{\"data\":{\"type\":\"ladder1v1Map\",\"relationships\":{\"mapVersion\":{\"data\":{\"type\":\"mapVersion\",\"id\":\"2\"}}}}}";

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotCreateLadderMapAsUser() throws Exception {
    mockMvc.perform(
      post("/data/ladder1v1Map")
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canCreateLadderMapAsModerator() throws Exception {
    mockMvc.perform(
      post("/data/ladder1v1Map")
        .content(NEW_LADDER_MAP_BODY)) // magic value from prepMapData.sql
      .andExpect(status().isCreated());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotDeleteLadderMapAsUser() throws Exception {
    mockMvc.perform(
      delete("/data/ladder1v1Map/1")) // magic value from prepMapData.sql
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canDeleteLadderMapAsModerator() throws Exception {
    mockMvc.perform(
      delete("/data/ladder1v1Map/1")) // magic value from prepMapData.sql
      .andExpect(status().isNoContent());
  }
}
