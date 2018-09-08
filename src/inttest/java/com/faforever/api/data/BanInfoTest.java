package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.email.EmailSender;
import com.faforever.api.player.PlayerRepository;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepBanData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanBanData.sql")
public class BanInfoTest extends AbstractIntegrationTest {
  /*
  {
      "data": {
          "type": "banInfo",
          "attributes": {
              "level": "CHAT",
              "reason": "This ban will be posted"
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
                      "id": "2"
                  }
              }
          }
      }
  }
   */
  private static final String testPost = "{\"data\":{\"type\":\"banInfo\",\"attributes\":{\"level\":\"CHAT\",\"reason\":\"This test ban should be revoked\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"player\",\"id\":\"2\"}},\"player\":{\"data\":{\"type\":\"player\",\"id\":\"3\"}}}}}";
  @Autowired
  PlayerRepository playerRepository;
  @MockBean
  private EmailSender emailSender;

  @Test
  @WithUserDetails(AUTH_USER)
  public void emptyResultBanInfoAsUser() throws Exception {
    mockMvc.perform(get("/data/banInfo"))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[]}"));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotReadSpecificBanInfoAsUser() throws Exception {
    mockMvc.perform(get("/data/banInfo/1"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotCreateBanInfoAsUser() throws Exception {
    mockMvc.perform(post("/data/banInfo")
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadBanInfoAsModerator() throws Exception {
    mockMvc.perform(get("/data/banInfo"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(2)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadSpecificBanInfoAsModerator() throws Exception {
    mockMvc.perform(get("/data/banInfo/1"))
      .andExpect(status().isOk());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canCreateBanInfoAsModerator() throws Exception {
    assertThat(playerRepository.getOne(3).getBans().size(), is(0));

    mockMvc.perform(post("/data/banInfo")
      .content(testPost))
      .andExpect(status().isCreated());

    assertThat(playerRepository.getOne(3).getBans().size(), is(1));
    Mockito.verify(emailSender).sendMail(ArgumentMatchers.eq("integration-test@faforever.com"),
      ArgumentMatchers.eq("integration-test@faforever.com"),
      ArgumentMatchers.eq("admin@faforever.com"),
      ArgumentMatchers.eq("ban subject"),
      ArgumentMatchers.matches("Hello ADMIN,\\|Your account was banned\\|Reason - This test ban should be revoked\\|Banner - MODERATOR\\|Time - (.)*\\|Type - CHAT\\|Expires - never\\|Thank you for your fairness and acceptance[.]")
    );
  }
}
