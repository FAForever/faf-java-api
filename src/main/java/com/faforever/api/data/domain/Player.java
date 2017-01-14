package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = "player")
@Data
public class Player extends Login {

  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;
  private List<ClanMembership> clanMemberships;

  @OneToOne(mappedBy = "player")
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player")
  public GlobalRating getGlobalRating() {
    return globalRating;
  }

  @OneToMany(mappedBy = "player")
  public List<ClanMembership> getClanMemberships() {
    return this.clanMemberships;
  }

  @Transient
  public Clan getClan() {
    if (getClanMemberships().size() == 1) {
      return getClanMemberships().get(0).getClan();
    }
    return null;
  }
}
