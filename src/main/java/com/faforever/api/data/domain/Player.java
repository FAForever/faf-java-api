package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.checks.permission.IsModerator;
import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = Player.TYPE_NAME)
// Needed to change leader of a clan
@SharePermission
@Getter
@Setter
@Type(Player.TYPE_NAME)
public class Player extends Login {
  public static final String TYPE_NAME = "player";

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @BatchSize(size = 1000)
  private Ladder1v1Rating ladder1v1Rating;

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @BatchSize(size = 1000)
  private GlobalRating globalRating;

  // Permission is managed by ClanMembership class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  private Set<ClanMembership> clanMemberships = new HashSet<>();

  // Permission is managed by NameRecord class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  private Set<NameRecord> names;

  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  private Set<AvatarAssignment> avatarAssignments;

  @ReadPermission(expression = IsModerator.EXPRESSION + " OR " + IsEntityOwner.EXPRESSION)
  // Permission is managed by Moderation reports class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "reporter")
  @BatchSize(size = 1000)
  private Set<ModerationReport> reporterOnModerationReports;

  // Permission is managed by Moderation reports class
  @ReadPermission(expression = IsModerator.EXPRESSION)
  @UpdatePermission(expression = Prefab.ALL)
  @ManyToMany(mappedBy = "reportedUsers")
  private Set<ModerationReport> reportedOnModerationReports;

  @Transient
  public Clan getClan() {
    return getClanMemberships().stream()
      .findFirst()
      .map(ClanMembership::getClan)
      .orElse(null);
  }

  @Override
  public String toString() {
    return "Player(" + getId() + ", " + getLogin() + ")";
  }
}
