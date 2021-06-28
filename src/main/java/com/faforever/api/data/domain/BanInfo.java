package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.hook.BanInfoCreateHook;
import com.faforever.api.data.hook.BanInfoRevokeAuthorUpdateHook;
import com.faforever.api.security.elide.permission.AdminAccountBanCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.LifeCycleHookBinding;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

import static com.yahoo.elide.annotation.LifeCycleHookBinding.Operation.CREATE;
import static com.yahoo.elide.annotation.LifeCycleHookBinding.Operation.UPDATE;
import static com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase.PRESECURITY;

@Entity
@Table(name = "ban")
@Include(name = "banInfo")
// Bans can never be deleted, only disabled over BanDisableData
@DeletePermission(expression = Prefab.NONE)
@ReadPermission(expression = AdminAccountBanCheck.EXPRESSION)
@CreatePermission(expression = AdminAccountBanCheck.EXPRESSION)
@UpdatePermission(expression = AdminAccountBanCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Applied ban with id `{0}` for player `{1}`", logExpressions = {"${banInfo.id}", "${banInfo.player}"})
@Audit(action = Action.UPDATE, logStatement = "Updated ban with id `{0}` for player `{1}`", logExpressions = {"${banInfo.id}", "${banInfo.player}"})
@LifeCycleHookBinding(operation = CREATE, phase = PRESECURITY, hook = BanInfoCreateHook.class)
@Data
@NoArgsConstructor
public class BanInfo implements DefaultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @ManyToOne
  @JoinColumn(name = "player_id")
  @NotNull
  private Player player;

  @ManyToOne
  @UpdatePermission(expression = Prefab.NONE)
  @JoinColumn(name = "author_id")
  @NotNull
  private Player author;

  @Column(name = "reason")
  @NotNull
  private String reason;

  @Column(name = "expires_at")
  private OffsetDateTime expiresAt;

  @Column(name = "level")
  @Enumerated(EnumType.STRING)
  private BanLevel level;

  @ManyToOne
  @JoinColumn(name = "report_id")
  @EqualsAndHashCode.Exclude
  private ModerationReport moderationReport;

  @Column(name = "revoke_reason")
  @LifeCycleHookBinding(operation = UPDATE, phase = PRESECURITY, hook = BanInfoRevokeAuthorUpdateHook.class)
  private String revokeReason;

  @ManyToOne
  @UpdatePermission(expression = Prefab.NONE)
  @JoinColumn(name = "revoke_author_id")
  private Player revokeAuthor;

  @Column(name = "revoke_time")
  @LifeCycleHookBinding(operation = UPDATE, phase = PRESECURITY, hook = BanInfoRevokeAuthorUpdateHook.class)
  private OffsetDateTime revokeTime;

  @Transient
  public BanDurationType getDuration() {
    return expiresAt == null ? BanDurationType.PERMANENT : BanDurationType.TEMPORARY;
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
}
