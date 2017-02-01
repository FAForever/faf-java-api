package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = "player")
public class Player extends Login {

  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;

  @Basic
  @Column(name = "clan_join_date")
  public Timestamp getClanJoinDate() {
    return clanJoinDate;
  }

  public void setClanJoinDate(Timestamp clanJoinDate) {
    this.clanJoinDate = clanJoinDate;
  }

  private Timestamp clanJoinDate;

  @OneToOne(mappedBy = "player")
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  public void setLadder1v1Rating(Ladder1v1Rating ladder1v1Rating) {
    this.ladder1v1Rating = ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player")
  public GlobalRating getGlobalRating() {
    return globalRating;
  }

  private Clan clan;

  @ManyToOne
  @JoinColumn(name = "clan_id")
  public Clan getClan() {
    return clan;
  }

  public void setClan(Clan newClan) { this.clan = newClan; }

  public void setGlobalRating(GlobalRating globalRating) {
    this.globalRating = globalRating;
  }
}
