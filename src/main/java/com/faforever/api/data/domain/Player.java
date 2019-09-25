package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.checks.permission.IsModerator;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = Player.TYPE_NAME)
// Needed to change leader of a clan
@SharePermission
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
  @BatchSize(size = 1000)
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @BatchSize(size = 1000)
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

  @ReadPermission(expression = IsModerator.EXPRESSION + " OR " + IsEntityOwner.EXPRESSION)
  // Permission is managed by Moderation reports class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "reporter")
  @BatchSize(size = 1000)
  public Set<ModerationReport> getReporterOnModerationReports() {
    return reporterOnModerationReports;
  }

  // Permission is managed by Moderation reports class
  @ReadPermission(expression = IsModerator.EXPRESSION)
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
