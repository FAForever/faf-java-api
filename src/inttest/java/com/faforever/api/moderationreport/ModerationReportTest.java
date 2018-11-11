package com.faforever.api.moderationreport;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.DataController;
import com.faforever.api.email.EmailService;
import com.faforever.commons.api.dto.Game;
import com.faforever.commons.api.dto.ModerationReport;
import com.faforever.commons.api.dto.ModerationReportStatus;
import com.faforever.commons.api.dto.Player;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepGameData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModerationReportData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanModerationReportData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanGameData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanMapData.sql")
public class ModerationReportTest extends AbstractIntegrationTest {
  private ModerationReport validModerationReport;
  private Set<Player> reportedUsers;
  @Autowired
  private FafApiProperties properties;

  @MockBean
  private EmailService emailService;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    Player reporter = (Player) new Player().setId("1");
    Player reportedUser1 = (Player) new Player().setId("3");
    Player reportedUser2 = (Player) new Player().setId("2");
    Game game = new Game().setId("1");
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
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanCreateValidModerationReport() throws Exception {
    mockMvc.perform(
      post("/data/moderationReport")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.AWAITING.name())))
      .andExpect(jsonPath("$.data.attributes.reportDescription", is("Report description")))
      .andExpect(jsonPath("$.data.attributes.gameIncidentTimecode", is("Incident code")))
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.AWAITING.name())))
      .andExpect(jsonPath("$.data.relationships.reporter.data.id", is("1")))
      .andExpect(jsonPath("$.data.relationships.game.data.id", is("1")))
      .andExpect(jsonPath("$.data.relationships.reportedUsers.data", hasSize(2)));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void newModerationReportNotifiesModerators() throws Exception {
    properties.getModerationReport()
      .setNotificationEmailSubject("Moderation Report notification")
      .setNotificationEmailBodyTemplate("New moderation report has been reported by {0}.\nDescription: {1}\nIncident code: {2}\nReported Users: {3}");
    mockMvc.perform(
      post("/data/moderationReport")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isCreated());
    verify(emailService, times(1)).sendMail(
      eq(Sets.newHashSet("admin@faforever.com", "moderator@faforever.com")),
      eq("Moderation Report notification"),
      eq("New moderation report has been reported by null.\nDescription: Report description\nIncident code: Incident code\nReported Users: [ADMIN, MODERATOR]"));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotCreateReportWithModeratorsData() throws Exception {
    validModerationReport
      .setModeratorNotice("Moderation notice")
      .setModeratorPrivateNote("Moderation private note");
    mockMvc.perform(
      post("/data/moderationReport")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotCreateReportWithoutReportedUsers() throws Exception {
    validModerationReport
      .setReportedUsers(null);
    mockMvc.perform(
      post("/data/moderationReport")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotCreateReportWithoutReportDescription() throws Exception {
    validModerationReport
      .setReportDescription(null);
    mockMvc.perform(
      post("/data/moderationReport")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(validModerationReport)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void moderationReportCannotBeDeletedByUser() throws Exception {
    mockMvc.perform(
      delete("/data/moderationReport/1"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderationReportCannotBeDeletedByModerator() throws Exception {
    mockMvc.perform(
      delete("/data/moderationReport/1"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanUpdateDescription() throws Exception {
    final ModerationReport updatedDescriptionModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setReportDescription("New report description")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedDescriptionModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.attributes.reportDescription", is("New report description")));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanUpdateTimecode() throws Exception {
    final ModerationReport updatedTimecodeModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setGameIncidentTimecode("New incident timecode")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedTimecodeModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.attributes.gameIncidentTimecode", is("New incident timecode")));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanUpdateGameId() throws Exception {
    final ModerationReport updatedGameIdModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setGame(new Game().setId("1"))
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedGameIdModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.relationships.game.data.id", is("1")));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanUpdateReportedUsers() throws Exception {
    reportedUsers.add((Player) new Player().setId("1"));
    final ModerationReport updatedReportedUsersModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setReportedUsers(reportedUsers)
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportedUsersModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.relationships.reportedUsers.data", hasSize(3)));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotUpdateReportInNonAwaitingState() throws Exception {
    reportedUsers.removeIf(player -> player.getId().equals("2"));
    final ModerationReport updatedGameIdModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(null)
      .setReportDescription("New report description")
      .setGameIncidentTimecode("New incident timecode")
      .setGame(new Game().setId("1"))
      .setReportedUsers(reportedUsers)
      .setId("2");
    mockMvc.perform(
      patch("/data/moderationReport/2")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedGameIdModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void reporterCannotUpdateReportStatus() throws Exception {
    final ModerationReport updatedReportStatusModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(ModerationReportStatus.AWAITING)
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportStatusModerationReport)))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanUpdateReportStatus() throws Exception {
    final ModerationReport updatedReportStatusModerationReport = (ModerationReport) new ModerationReport()
      .setReportStatus(ModerationReportStatus.COMPLETED)
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportStatusModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.COMPLETED.name())));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanUpdateNotice() throws Exception {
    final ModerationReport updatedModeratorNoticeModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorNotice("New moderator notice")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedModeratorNoticeModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.attributes.moderatorNotice", is("New moderator notice")));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanUpdatePrivateNote() throws Exception {
    final ModerationReport updatedModeratorPrivateNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorPrivateNote("New moderator private note")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedModeratorPrivateNoteModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.attributes.moderatorPrivateNote", is("New moderator private note")));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void lastModeratorIsUpdatedWhenCalledByModerator() throws Exception {
    final ModerationReport updatedModeratorPrivateNoteModerationReport = (ModerationReport) new ModerationReport()
      .setModeratorPrivateNote("New moderator private note")
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedModeratorPrivateNoteModerationReport)))
      .andExpect(status().isNoContent());
    mockMvc.perform(
      get("/data/moderationReport/1"))
      .andExpect(jsonPath("$.data.relationships.lastModerator.data.id", is("2")));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanSeeOwnFieldsAndReportStatusAndModeratorNotice() throws Exception {
    mockMvc.perform(
      get("/data/moderationReport/2"))
      .andExpect(jsonPath("$.data.attributes.reportDescription", is("Report description")))
      .andExpect(jsonPath("$.data.attributes.gameIncidentTimecode", is("Incident timecode")))
      .andExpect(jsonPath("$.data.attributes.reportStatus", is(ModerationReportStatus.PROCESSING.name())))
      .andExpect(jsonPath("$.data.attributes.moderatorNotice", is("Moderator notice")));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCannotSeeModeratorPrivateNote() throws Exception {
    mockMvc.perform(
      get("/data/moderationReport/2"))
      .andExpect(jsonPath("$.data.attributes.moderatorPrivateNote").doesNotExist());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void userCanSeeOnlyOwnReports() throws Exception {
    mockMvc.perform(
      get("/data/moderationReport"))
      .andExpect(jsonPath("$.data[*].relationships.reporter.data.id", everyItem(is("1"))));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCannotUpdateReportedUsers() throws Exception {
    final ModerationReport updatedReportedUsersModerationReport = (ModerationReport) new ModerationReport()
      .setReportedUsers(Collections.emptySet())
      .setId("1");
    mockMvc.perform(
      patch("/data/moderationReport/1")
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(updatedReportedUsersModerationReport)))
      .andExpect(status().isForbidden());
  }
}
