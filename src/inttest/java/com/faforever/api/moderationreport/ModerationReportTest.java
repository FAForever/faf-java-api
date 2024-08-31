package com.faforever.api.moderationreport;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.faforever.commons.api.dto.Game;
import com.faforever.commons.api.dto.ModerationReport;
import com.faforever.commons.api.dto.ModerationReportStatus;
import com.faforever.commons.api.dto.Player;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.Set;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
public class ModerationReportTest extends AbstractIntegrationTest {
  private ModerationReport validModerationReport;
  private Set<Player> reportedUsers;

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
    Player reporter = (Player) new Player().setId("1");
    Player reportedUser1 = (Player) new Player().setId("3");
    Player reportedUser2 = (Player) new Player().setId("2");
    Game game = (Game) new Game().setId("1");
    reportedUsers = Sets.newHashSet(reportedUser1, reportedUser2);
    validModerationReport = new ModerationReport()
      .setReportDescription("Report description")
      .setGameIncidentTimecode("Incident code")
      .setGame(game)
      .setReportStatus(null)
      .setReporter(reporter)
      .setReportedUsers(reportedUsers)
    ;
  }

  @Test
  @WithAnonymousUser
  public void anonymousUserCannotCreateValidModerationReport() throws Exception {
    mockMvc.perform(
      post("/data/moderationReport")
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateValidModerationReportWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/account"));
    mockMvc.perform(
                   post("/data/moderationReport")
                           .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
                           .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
                           .content(createJsonApiContent(validModerationReport)))
           .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateValidModerationReportWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/account"));
    mockMvc.perform(
      post("/data/moderationReport")
        .with(getOAuthTokenWithActiveUser(OAuthScope._LOBBY, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.AWAITING.name())))
      .andExpect(jsonPath("$.data.attributes.reportDescription", is("Report description")))
      .andExpect(jsonPath("$.data.attributes.gameIncidentTimecode", is("Incident code")))
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.AWAITING.name())))
      .andExpect(jsonPath("$.data.relationships.reporter.data.id", is("5")))
      .andExpect(jsonPath("$.data.relationships.game.data.id", is("1")))
      .andExpect(jsonPath("$.data.relationships.reportedUsers.data", hasSize(2)));
  }

  @Test
  public void cannotCreateReportWithModeratorsDataWithoutScopeAndRole() throws Exception {
    validModerationReport
      .setModeratorNotice("Moderation notice")
      .setModeratorPrivateNote("Moderation private note");
    mockMvc.perform(
      post("/data/moderationReport")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateReportWithoutReportedUsers() throws Exception {
    validModerationReport
      .setReportedUsers(null);
    mockMvc.perform(
      post("/data/moderationReport")
        .with(getOAuthTokenWithActiveUser(OAuthScope._LOBBY, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void userCannotCreateReportWithoutReportDescription() throws Exception {
    validModerationReport
      .setReportDescription(null);
    mockMvc.perform(
      post("/data/moderationReport")
        .with(getOAuthTokenWithActiveUser(OAuthScope._LOBBY, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void cannotDeleteReportWithoutScopeAndRole() throws Exception {
    mockMvc.perform(
      delete("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void moderationReportCannotBeDeletedEvenWithScopeAndRole() throws Exception {
    mockMvc.perform(
      delete("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_NOTE)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateSomeoneElsesReport() throws Exception {
    reportedUsers.add((Player) new Player().setId("2"));

    final ModerationReport updatedModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setReportDescription("New report description")
      .setId("3");
    mockMvc.perform(
      patch("/data/moderationReport/3")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedModerationReport)))
      .andExpect(status().isNotFound());
  }

  @Test
  public void canUpdateOwnReport() throws Exception {
    reportedUsers.add((Player) new Player().setId("1"));

    final ModerationReport updatedModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setReportDescription("New report description")
      .setGameIncidentTimecode("New incident timecode")
      .setGame((Game) new Game().setId("1"))
      .setReportedUsers(reportedUsers)
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(jsonPath("$.data.attributes.reportDescription", is("New report description")))
      .andExpect(jsonPath("$.data.attributes.gameIncidentTimecode", is("New incident timecode")))
      .andExpect(jsonPath("$.data.relationships.game.data.id", is("1")))
      .andExpect(jsonPath("$.data.relationships.reportedUsers.data", hasSize(3)));
  }

  @Test
  public void userCannotUpdateReportInNonAwaitingState() throws Exception {
    reportedUsers.removeIf(player -> player.getId().equals("2"));
    final ModerationReport updatedGameIdModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setReportDescription("New report description")
      .setGameIncidentTimecode("New incident timecode")
      .setGame((Game) new Game().setId("1"))
      .setReportedUsers(reportedUsers)
      .setId("2");
    mockMvc.perform(
      patch("/data/moderationReport/2")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedGameIdModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateReportStatusWithoutScope() throws Exception {
    final ModerationReport updatedReportStatusModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(ModerationReportStatus.AWAITING)
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportStatusModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateReportStatusWithoutRole() throws Exception {
    final ModerationReport updatedReportStatusModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(ModerationReportStatus.AWAITING)
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportStatusModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canUpdateReportStatusWithScopeAndRole() throws Exception {
    final ModerationReport updatedReportStatusModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(ModerationReportStatus.AWAITING)
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportStatusModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT)))
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.AWAITING.name())));
  }

  @Test
  public void canUpdateNoticeWithScopeAndRole() throws Exception {
    final ModerationReport updatedPublicNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorNotice("New moderator notice")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedPublicNoteModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT)))
      .andExpect(jsonPath("$.data.attributes.moderatorNotice", is("New moderator notice")));
  }

  @Test
  public void cannotUpdateNoticeWithoutScope() throws Exception {
    final ModerationReport updatedPublicNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorNotice("New moderator notice")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedPublicNoteModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateNoticeWithoutRole() throws Exception {
    final ModerationReport updatedPublicNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorNotice("New moderator notice")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedPublicNoteModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canUpdatePrivateNoteWithScopeAndRole() throws Exception {
    final ModerationReport updatedPrivateNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorPrivateNote("New moderator private note")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedPrivateNoteModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT)))
      .andExpect(jsonPath("$.data.attributes.moderatorPrivateNote", is("New moderator private note")));
  }

  @Test
  public void cannotUpdatePrivateNoteWithoutScope() throws Exception {
    final ModerationReport updatedPrivateNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorPrivateNote("New moderator private note")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedPrivateNoteModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdatePrivateNoteWithoutRole() throws Exception {
    final ModerationReport updatedPrivateNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorPrivateNote("New moderator private note")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedPrivateNoteModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void lastModeratorIsUpdatedWhenCalledByModerator() throws Exception {
    final ModerationReport updatedPrivateNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorPrivateNote("New moderator private note")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedPrivateNoteModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT)))
      .andExpect(jsonPath("$.data.relationships.lastModerator.data.id", is("5")));
  }

  @Test
  public void userCanSeeOwnFieldsAndReportStatusAndPublicNote() throws Exception {
    mockMvc.perform(
      get("/data/moderationReport/2")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.reportDescription", is("Report description")))
      .andExpect(jsonPath("$.data.attributes.gameIncidentTimecode", is("Incident timecode")))
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.PROCESSING.name())))
      .andExpect(jsonPath("$.data.attributes.moderatorNotice", is("Moderator notice")));
  }

  @Test
  public void userCannotSeePrivateNote() throws Exception {
    mockMvc.perform(
      get("/data/moderationReport/2")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(jsonPath("$.data.attributes.moderatorPrivateNote").doesNotExist());
  }

  @Test
  public void userCanSeeOnlyOwnReports() throws Exception {
    mockMvc.perform(
      get("/data/moderationReport")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(jsonPath("$.data[*].relationships.reporter.data.id", everyItem(is("5"))));
  }

  @Test
  public void cannotUpdateReportedUsers() throws Exception {
    final ModerationReport updatedReportedUsersModerationReport = (ModerationReport) new ModerationReport()
      .setReportedUsers(Set.of())
      .setId("3");
    mockMvc.perform(
      patch("/data/moderationReport/3")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_MODERATION_REPORT))
        .header(HttpHeaders.CONTENT_TYPE, JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportedUsersModerationReport)))
      .andExpect(status().isForbidden());
  }
}
