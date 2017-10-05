package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
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
import java.util.List;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = "player")
// Needed to change leader of a clan
@SharePermission(expression = "Prefab.Role.All")
@Setter
public class Player extends Login {

  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;
  private List<ClanMembership> clanMemberships;
  private List<AvatarAssignment> avatarAssignments;
  private List<HardwareInformation> hardwareInformations;

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
  @UpdatePermission(expression = "Prefab.Role.All")
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  public List<ClanMembership> getClanMemberships() {
    return this.clanMemberships;
  }

  @Transient
  public Clan getClan() {
    if (getClanMemberships() != null && getClanMemberships().size() == 1) {
      return getClanMemberships().get(0).getClan();
    }
    return null;
  }

  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = "Prefab.Role.All")
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  public List<AvatarAssignment> getAvatarAssignments() {
    return avatarAssignments;
  }

  @ManyToMany(mappedBy = "players")
  public List<HardwareInformation> getHardwareInformations() {
    return this.hardwareInformations;
  }

  @Override
  public String toString() {
    return "Player(" + getId() + ", " + getLogin() + ")";
  }
}
