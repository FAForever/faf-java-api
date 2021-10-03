package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.IsInAwaitingState;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.hook.ModerationReportHook;
import com.faforever.api.security.elide.permission.AdminModerationReportCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.LifeCycleHookBinding;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;

import static com.yahoo.elide.annotation.LifeCycleHookBinding.Operation.CREATE;
import static com.yahoo.elide.annotation.LifeCycleHookBinding.Operation.UPDATE;
import static com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase.PRESECURITY;

@Entity
@Table(name = "moderation_report")
@Setter
@ToString(exclude = {"reportedUsers", "bans"})
@Include(name = ModerationReport.TYPE_NAME)
@ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + AdminModerationReportCheck.EXPRESSION)
@DeletePermission(expression = Prefab.NONE)
@CreatePermission(expression = Prefab.ALL)
@Audit(action = Action.CREATE, logStatement = "Moderation report `{0}` has been reported", logExpressions = "${moderationReport}")
@Audit(action = Action.UPDATE, logStatement = "Moderation report `{0}` has been updated", logExpressions = "${moderationReport}")
@LifeCycleHookBinding(operation = CREATE, phase = PRESECURITY, hook = ModerationReportHook.class)
@LifeCycleHookBinding(operation = UPDATE, phase = PRESECURITY, hook = ModerationReportHook.class)
public class ModerationReport extends AbstractEntity<ModerationReport> implements OwnableEntity {
  public static final String TYPE_NAME = "moderationReport";
  private ModerationReportStatus reportStatus;
  private Player reporter;
  private String reportDescription;
  private String gameIncidentTimecode;
  private Game game;
  private String moderatorNotice;
  private String moderatorPrivateNote;
  private Player lastModerator;
  private Set<Player> reportedUsers;
  private Collection<BanInfo> bans;

  @NotNull
  @Column(name = "report_status")
  @Enumerated(EnumType.STRING)
  @CreatePermission(expression = Prefab.NONE)
  @UpdatePermission(expression = AdminModerationReportCheck.EXPRESSION)
  public ModerationReportStatus getReportStatus() {
    return reportStatus;
  }

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", referencedColumnName = "id")
  @CreatePermission(expression = Prefab.ALL)
  public Player getReporter() {
    return reporter;
  }

  @NotNull
  @Column(name = "report_description")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION + " and " + IsInAwaitingState.EXPRESSION)
  public String getReportDescription() {
    return reportDescription;
  }

  @Column(name = "game_incident_timecode")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION + " and " + IsInAwaitingState.EXPRESSION)
  public String getGameIncidentTimecode() {
    return gameIncidentTimecode;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id", referencedColumnName = "id")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION + " and " + IsInAwaitingState.EXPRESSION)
  public Game getGame() {
    return game;
  }

  @Column(name = "moderator_notice")
  @CreatePermission(expression = Prefab.NONE)
  @UpdatePermission(expression = AdminModerationReportCheck.EXPRESSION)
  public String getModeratorNotice() {
    return moderatorNotice;
  }

  @Column(name = "moderator_private_note")
  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION)
  @CreatePermission(expression = Prefab.NONE)
  @UpdatePermission(expression = AdminModerationReportCheck.EXPRESSION)
  public String getModeratorPrivateNote() {
    return moderatorPrivateNote;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_moderator", referencedColumnName = "id")
  @CreatePermission(expression = Prefab.NONE)
  @UpdatePermission(expression = AdminModerationReportCheck.EXPRESSION)
  public Player getLastModerator() {
    return lastModerator;
  }

  @Size(min = 1)
  @NotNull
  @Valid
  @ManyToMany(cascade = {
    CascadeType.PERSIST,
    CascadeType.MERGE
  })
  @JoinTable(name = "reported_user",
    joinColumns = @JoinColumn(name = "report_id"),
    inverseJoinColumns = @JoinColumn(name = "player_id")
  )
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION + " and " + IsInAwaitingState.EXPRESSION)
  public Set<Player> getReportedUsers() {
    return reportedUsers;
  }

  @OneToMany(mappedBy = "moderationReport")
  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION)
  // Permission is managed by BanInfo class
  @UpdatePermission(expression = Prefab.ALL)
  public Collection<BanInfo> getBans() {
    return bans;
  }

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return getReporter();
  }
}
