package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import com.yahoo.elide.security.checks.prefab.Role;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = "player")
@SharePermission(any = Role.ALL.class) // Needed e.g. to change leader of a clan
@Setter
public class Player extends Login {

  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;
  private List<ClanMembership> clanMemberships;
  private List<BanInfo> bans;
  private List<BanInfo> createdBans;

  @OneToOne(mappedBy = "player")
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player")
  public GlobalRating getGlobalRating() {
    return globalRating;
  }

  @OneToMany(mappedBy = "player")
  @UpdatePermission(any = {Role.ALL.class}) // Permission is managed by ClanMembership class
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

  @OneToMany(mappedBy = "player")
  @UpdatePermission(any = {Role.ALL.class}) // Permission is managed by BanInfo class
  public List<BanInfo> getBans() {
    return this.bans;
  }

  @Transient
  public List<BanInfo> getActiveBans() {
    return getBans().stream().filter(ban -> ban.getBanStatus() == BanStatusType.BANNED).collect(Collectors.toList());
  }

  @Transient
  public boolean isGlobalBanned() {
    return getActiveBans().stream().anyMatch(ban -> ban.getType() == BanType.GLOBAL);
  }

  @OneToMany(mappedBy = "author")
  @UpdatePermission(any = {Role.ALL.class}) // Permission is managed by BanInfo class
  public List<BanInfo> getCreatedBans() {
    return this.createdBans;
  }
}
