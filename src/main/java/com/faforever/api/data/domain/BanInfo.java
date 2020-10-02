package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.FafUserDetails;
import com.faforever.api.security.elide.permission.AdminAccountBanCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.OnCreatePreSecurity;
import com.yahoo.elide.annotation.OnUpdatePreSecurity;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import com.yahoo.elide.core.RequestScope;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ban")
@Include(rootLevel = true, type = "banInfo")
// Bans can never be deleted, only disabled over BanDisableData
@DeletePermission(expression = Prefab.NONE)
@ReadPermission(expression = AdminAccountBanCheck.EXPRESSION)
@CreatePermission(expression = AdminAccountBanCheck.EXPRESSION)
@UpdatePermission(expression = AdminAccountBanCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Applied ban with id `{0}` for player `{1}`", logExpressions = {"${banInfo.id}", "${banInfo.player}"})
@Audit(action = Action.UPDATE, logStatement = "Updated ban with id `{0}` for player `{1}`", logExpressions = {"${banInfo.id}", "${banInfo.player}"})
@Setter
public class BanInfo extends AbstractEntity {
  private Player player;
  private Player author;
  private String reason;
  private OffsetDateTime expiresAt;
  private BanLevel level;
  private ModerationReport moderationReport;
  private String revokeReason;
  private Player revokeAuthor;
  private OffsetDateTime revokeTime;

  @ManyToOne
  @JoinColumn(name = "player_id")
  @NotNull
  public Player getPlayer() {
    return player;
  }

  @ManyToOne
  @UpdatePermission(expression = Prefab.NONE)
  @JoinColumn(name = "author_id")
  @NotNull
  public Player getAuthor() {
    return author;
  }

  @Column(name = "reason")
  @NotNull
  public String getReason() {
    return reason;
  }

  @Column(name = "expires_at")
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  @Column(name = "level")
  @Enumerated(EnumType.STRING)
  public BanLevel getLevel() {
    return level;
  }

  @ManyToOne
  @JoinColumn(name = "report_id")
  public ModerationReport getModerationReport() {
    return moderationReport;
  }

  @Transient
  public BanDurationType getDuration() {
    return expiresAt == null ? BanDurationType.PERMANENT : BanDurationType.TEMPORARY;
  }

  @Column(name = "revoke_reason")
  public String getRevokeReason() {
    return revokeReason;
  }

  @ManyToOne
  @UpdatePermission(expression = Prefab.NONE)
  @JoinColumn(name = "revoke_author_id")
  public Player getRevokeAuthor() {
    return revokeAuthor;
  }

  @Column(name = "revoke_time")
  public OffsetDateTime getRevokeTime() {
    return revokeTime;
  }

  @Transient
  public BanStatus getBanStatus() {
    if (revokeTime != null && revokeTime.isBefore(OffsetDateTime.now())) {
      return BanStatus.DISABLED;
    }
    if (getDuration() == BanDurationType.PERMANENT) {
      return BanStatus.BANNED;
    }
    return expiresAt.isAfter(OffsetDateTime.now())
      ? BanStatus.BANNED
      : BanStatus.EXPIRED;
  }

  @OnCreatePreSecurity
  public void assignReporter(RequestScope scope) {
    final Object caller = scope.getUser().getOpaqueUser();
    if (caller instanceof FafUserDetails) {
      final FafUserDetails fafUser = (FafUserDetails) caller;
      final Player author = new Player();
      author.setId(fafUser.getId());
      this.author = author;
    }
  }

  @OnUpdatePreSecurity("revokeTime")
  public void revokeTimeUpdated(RequestScope scope) {
    assignRevokeAuthor(scope);
  }

  @OnUpdatePreSecurity("revokeReason")
  public void revokeReasonUpdated(RequestScope scope) {
    assignRevokeAuthor(scope);
  }

  private void assignRevokeAuthor(RequestScope scope) {
    final Object caller = scope.getUser().getOpaqueUser();
    if (caller instanceof FafUserDetails) {
      final FafUserDetails fafUser = (FafUserDetails) caller;
      final Player author = new Player();
      author.setId(fafUser.getId());
      this.revokeAuthor = author;
    }
  }
}
