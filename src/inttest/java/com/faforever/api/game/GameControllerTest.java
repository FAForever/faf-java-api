package com.faforever.api.game;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameControllerTest extends AbstractIntegrationTest {
  @Test
  public void testDownloadRedirect() throws Exception {
    this.mockMvc.perform(get("/game/11689995/replay")
    ).andExpect(status().isFound())
      .andExpect(redirectedUrl("http://localhost/replays/0/11/68/99/11689995.fafreplay"));
  }
}
