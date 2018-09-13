package com.faforever.api.leaderboard;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepLeaderboardData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanLeaderboardData.sql")
public class LeaderboardControllerTest extends AbstractIntegrationTest {

  @Test
  public void ladder1v1All() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/leaderboards/ladder1v1"))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[{\"type\":\"ladder1v1LeaderboardEntry\",\"id\":\"1\",\"attributes\":{\"name\":\"USER\",\"mean\":1500.0,\"deviation\":120.0,\"numGames\":5,\"wonGames\":1,\"rank\":1,\"rating\":1140}},{\"type\":\"ladder1v1LeaderboardEntry\",\"id\":\"2\",\"attributes\":{\"name\":\"MODERATOR\",\"mean\":1200.0,\"deviation\":90.0,\"numGames\":5,\"wonGames\":2,\"rank\":2,\"rating\":930}},{\"type\":\"ladder1v1LeaderboardEntry\",\"id\":\"3\",\"attributes\":{\"name\":\"ADMIN\",\"mean\":1000.0,\"deviation\":100.0,\"numGames\":5,\"wonGames\":3,\"rank\":3,\"rating\":700}}]}"));
  }

  @Test
  public void ladder1v1FirstPage() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/leaderboards/ladder1v1?page[size]=1"))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[{\"type\":\"ladder1v1LeaderboardEntry\",\"id\":\"1\",\"attributes\":{\"name\":\"USER\",\"mean\":1500.0,\"deviation\":120.0,\"numGames\":5,\"wonGames\":1,\"rank\":1,\"rating\":1140}}]}"));
  }

  @Test
  public void ladder1v1SecondPage() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/leaderboards/ladder1v1?page[size]=1&page[number]=2"))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[{\"type\":\"ladder1v1LeaderboardEntry\",\"id\":\"2\",\"attributes\":{\"name\":\"MODERATOR\",\"mean\":1200.0,\"deviation\":90.0,\"numGames\":5,\"wonGames\":2,\"rank\":2,\"rating\":930}}]}"));
  }

  @Test
  public void globalAll() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/leaderboards/global"))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[{\"type\":\"globalLeaderboardEntry\",\"id\":\"1\",\"attributes\":{\"name\":\"USER\",\"mean\":1500.0,\"deviation\":120.0,\"numGames\":5,\"rank\":1,\"rating\":1140}},{\"type\":\"globalLeaderboardEntry\",\"id\":\"2\",\"attributes\":{\"name\":\"MODERATOR\",\"mean\":1200.0,\"deviation\":90.0,\"numGames\":5,\"rank\":2,\"rating\":930}},{\"type\":\"globalLeaderboardEntry\",\"id\":\"3\",\"attributes\":{\"name\":\"ADMIN\",\"mean\":1000.0,\"deviation\":100.0,\"numGames\":5,\"rank\":3,\"rating\":700}}]}"));
  }

  @Test
  public void globalFirstPage() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/leaderboards/global?page[size]=1"))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[{\"type\":\"globalLeaderboardEntry\",\"id\":\"1\",\"attributes\":{\"name\":\"USER\",\"mean\":1500.0,\"deviation\":120.0,\"numGames\":5,\"rank\":1,\"rating\":1140}}]}"));
  }

  @Test
  public void globalSecondPage() throws Exception {
    MvcResult mvcResult = mockMvc.perform(get("/leaderboards/global?page[size]=1&page[number]=2"))
      .andReturn();

    mockMvc.perform(asyncDispatch(mvcResult))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[{\"type\":\"globalLeaderboardEntry\",\"id\":\"2\",\"attributes\":{\"name\":\"MODERATOR\",\"mean\":1200.0,\"deviation\":90.0,\"numGames\":5,\"rank\":2,\"rating\":930}}]}"));
  }
}
