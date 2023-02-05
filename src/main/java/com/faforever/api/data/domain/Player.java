package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.AdminModerationReportCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
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
  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;
  private ClanMembership clanMembership;
  private Set<NameRecord> names;
  private Set<AvatarAssignment> avatarAssignments;
  private Set<ModerationReport> reporterOnModerationReports;
  private Set<ModerationReport> reportedOnModerationReports;

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @Deprecated
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @Deprecated
  public GlobalRating getGlobalRating() {
    return globalRating;
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
