package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.AdminModerationReportCheck;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@Include(name = Player.TYPE_NAME)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Type(Player.TYPE_NAME)
public class Player extends Login {

  public static final String TYPE_NAME = "player";

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @Deprecated
  private Ladder1v1Rating ladder1v1Rating;

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @Deprecated
  private GlobalRating globalRating;

  // Permission is managed by ClanMembership class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToOne(mappedBy = "player")
  private ClanMembership clanMembership;

  // Permission is managed by NameRecord class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  private Set<NameRecord> names;

  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  private Set<AvatarAssignment> avatarAssignments;

  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION + " OR " + IsEntityOwner.EXPRESSION)
  // Permission is managed by Moderation reports class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "reporter")
  @BatchSize(size = 1000)
  private Set<ModerationReport> reporterOnModerationReports;

  // Permission is managed by Moderation reports class
  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION)
  @UpdatePermission(expression = Prefab.ALL)
  @ManyToMany(mappedBy = "reportedUsers")
  private Set<ModerationReport> reportedOnModerationReports;

  @Transient
  public Clan getClan() {
    return clanMembership == null ? null : clanMembership.getClan();
  }

  @Override
  public String toString() {
    return "Player(" + getId() + ", " + getLogin() + ")";
  }
}
