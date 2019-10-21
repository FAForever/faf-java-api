package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.player.PlayerRepository;
import com.faforever.commons.api.dto.BanInfo;
import com.faforever.commons.api.dto.BanLevel;
import com.faforever.commons.api.dto.ModerationReport;
import com.faforever.commons.api.dto.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepGameData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModerationReportData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepBanData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanBanData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanModerationReportData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanGameData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanMapData.sql")
public class BanTest extends AbstractIntegrationTest {
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
                      "id": "3"
                  }
              }
          }
      }
  }
   */
  private static final String testPost = "{\"data\":{\"type\":\"banInfo\",\"attributes\":{\"level\":\"CHAT\",\"reason\":\"This test ban should be revoked\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"player\",\"id\":\"1\"}},\"player\":{\"data\":{\"type\":\"player\",\"id\":\"3\"}}}}}";
  @Autowired
  PlayerRepository playerRepository;

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
      .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadBanInfoAsModerator() throws Exception {
    mockMvc.perform(get("/data/banInfo"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(3)));
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
      .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isCreated());

    assertThat(playerRepository.getOne(3).getBans().size(), is(1));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canCreateBanInfoBasedOnModerationReportAsModerator() throws Exception {
    final BanInfo banInfo = new BanInfo()
      .setLevel(BanLevel.CHAT)
      .setReason("Ban reason")
      .setPlayer((Player) new Player().setId("3"))
      .setModerationReport((ModerationReport) new ModerationReport().setId("1"));
    mockMvc.perform(post("/data/banInfo")
      .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
      .content(createJsonApiContent(banInfo)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.relationships.moderationReport.data.id", is("1")));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanRevokeBan() throws Exception {
    final BanInfo banInfo = new BanInfo();
    banInfo.setId("3");
    banInfo.setRevokeTime(OffsetDateTime.now());
    banInfo.setRevokeReason("revoke reason");

    mockMvc.perform(patch("/data/banInfo/3")
      .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
      .content(createJsonApiContent(banInfo)))
      .andExpect(status().isNoContent());

    assertThat(playerRepository.getOne(5).isGlobalBanned(), is(false));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanNotRevokeBan() throws Exception {
    final BanInfo banInfo = new BanInfo();
    banInfo.setId("3");
    banInfo.setRevokeTime(OffsetDateTime.now());
    banInfo.setRevokeReason("new revoke reason");

    mockMvc.perform(patch("/data/banInfo/3")
      .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
      .content(createJsonApiContent(banInfo)))
      .andExpect(status().isForbidden());
  }
}
