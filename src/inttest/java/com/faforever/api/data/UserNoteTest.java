package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.player.PlayerRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
  @WithUserDetails(AUTH_USER)
  public void emptyResultUserNoteAsUser() throws Exception {
    mockMvc.perform(get("/data/userNote"))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[]}"));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotReadSpecificUserNoteAsUser() throws Exception {
    mockMvc.perform(get("/data/userNote/1"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotCreateUserNoteAsUser() throws Exception {
    mockMvc.perform(post("/data/userNote")
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadUserNoteAsModerator() throws Exception {
    mockMvc.perform(get("/data/userNote"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadSpecificUserNoteAsModerator() throws Exception {
    mockMvc.perform(get("/data/userNote/1"))
      .andExpect(status().isOk());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canCreateUserNoteAsModerator() throws Exception {
    assertThat(playerRepository.getOne(3).getUserNotes().size(), is(0));

    mockMvc.perform(post("/data/userNote")
      .content(testPost))
      .andExpect(status().isCreated());

    assertThat(playerRepository.getOne(3).getUserNotes().size(), is(1));
  }
}
