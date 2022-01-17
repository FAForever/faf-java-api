package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.faforever.commons.api.dto.BanInfo;
import com.faforever.commons.api.dto.BanLevel;
import com.faforever.commons.api.dto.ModerationReport;
import com.faforever.commons.api.dto.Player;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.OffsetDateTime;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static com.faforever.api.data.domain.GroupPermission.ROLE_ADMIN_ACCOUNT_BAN;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepGameData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModerationReportData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepBanData.sql")
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

  @Test
  public void cannotReadSpecificBanWithoutScope() throws Exception {
    mockMvc.perform(get("/data/banInfo/1")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, ROLE_ADMIN_ACCOUNT_BAN)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotReadSpecificBanWithoutRole() throws Exception {
    mockMvc.perform(get("/data/banInfo/1")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canReadSpecificBanWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/banInfo/1")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_BAN)))
      .andExpect(status().isOk());
  }

  @Test
  public void canReadBansWithoutScope() throws Exception {
    mockMvc.perform(get("/data/banInfo")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, ROLE_ADMIN_ACCOUNT_BAN)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canReadBansWithoutRole() throws Exception {
    mockMvc.perform(get("/data/banInfo")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canReadBansWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/banInfo")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_BAN)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(3)));
  }

  @Test
  public void cannotCreateBanWithoutScope() throws Exception {
    mockMvc.perform(post("/data/banInfo")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, ROLE_ADMIN_ACCOUNT_BAN))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateBanWithoutRole() throws Exception {
    mockMvc.perform(post("/data/banInfo")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateBanWithScopeAndRole() throws Exception {
    mockMvc.perform(post("/data/banInfo")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_BAN))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(testPost))
      .andExpect(status().isCreated());
  }

  @Test
  public void canRevokeBanWithScopeAndRole() throws Exception {
    final BanInfo banToRevoke = (BanInfo) new BanInfo()
      .setRevokeReason("Revoke reason")
      .setRevokeTime(OffsetDateTime.now())
      .setId("3");
    mockMvc.perform(patch("/data/banInfo/3")
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_BAN))
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .content(createJsonApiContent(banToRevoke)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/banInfo/3")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_BAN)))
      .andExpect(jsonPath("$.data.attributes.revokeReason", Matchers.is("Revoke reason")))
      .andExpect(jsonPath("$.data.relationships.revokeAuthor.data.id", Matchers.is("5")));
  }

  @Test
  public void canCreateBanWithModerationWithScopeAndRole() throws Exception {

    final BanInfo ban = new BanInfo()
      .setLevel(BanLevel.CHAT)
      .setReason("Ban reason")
      .setPlayer((Player) new Player().setId("3"))
      .setModerationReport((ModerationReport) new ModerationReport().setId("1"));

    mockMvc.perform(post("/data/banInfo")
      .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
      .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_BAN))
      .content(createJsonApiContent(ban)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.relationships.player.data.id", is("3")));
  }
}
