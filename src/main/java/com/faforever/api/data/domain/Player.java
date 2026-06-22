package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.hook.PlayerAvatarUpdateHook;
import com.faforever.api.security.elide.permission.AdminModerationReportCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.LifeCycleHookBinding;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Set;

@Entity
@Table(name = "login")
@Include(name = Player.TYPE_NAME)
@Setter
@Type(Player.TYPE_NAME)
public class Player extends Login {

  public static final String TYPE_NAME = "player";
  private ClanMembership clanMembership;
  private Set<NameRecord> names;
  private Set<AvatarAssignment> avatarAssignments;
  private Avatar currentAvatar;
  private Set<ModerationReport> reporterOnModerationReports;
  private Set<ModerationReport> reportedOnModerationReports;

  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  @LifeCycleHookBinding(
    operation = LifeCycleHookBinding.Operation.UPDATE,
    phase = LifeCycleHookBinding.TransactionPhase.POSTCOMMIT,
    hook = PlayerAvatarUpdateHook.class
  )
  @Audit(action = Action.UPDATE, logStatement = "Avatar ''{0}'' has been selected on player ''{1}''", logExpressions = {"${player.currentAvatar.id}", "${player.id}"})
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "avatar_id")
  public Avatar getCurrentAvatar() {
    return currentAvatar;
  }

  // Permission is managed by ClanMembership class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToOne(mappedBy = "player")
  public ClanMembership getClanMembership() {
    return this.clanMembership;
  }

  @Transient
  public Clan getClan() {
    return clanMembership == null ? null : clanMembership.getClan();
  }

  // Permission is managed by NameRecord class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  public Set<NameRecord> getNames() {
    return this.names;
  }

  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  public Set<AvatarAssignment> getAvatarAssignments() {
    return avatarAssignments;
  }

  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION + " OR " + IsEntityOwner.EXPRESSION)
  // Permission is managed by Moderation reports class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "reporter")
  @BatchSize(size = 1000)
  public Set<ModerationReport> getReporterOnModerationReports() {
    return reporterOnModerationReports;
  }

  // Permission is managed by Moderation reports class
  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION)
  @UpdatePermission(expression = Prefab.ALL)
  @ManyToMany(mappedBy = "reportedUsers")
  public Set<ModerationReport> getReportedOnModerationReports() {
    return reportedOnModerationReports;
  }

  @Override
  public String toString() {
    return "Player(" + getId() + ", " + getLogin() + ")";
  }
}
