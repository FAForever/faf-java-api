package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.ComputedRelationship;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "clan_members")
@IdClass(ClanMembershipPK.class)
@Include(type = "clan_membership")
public class ClanMembership {

  private int clanId;
  private int playerId;
  private Timestamp joinClanDate;
  private Clan clan;
  private Player player;

  @Id
  @Column(name = "clan_id", insertable = false, updatable = false)
  @Exclude
  public int getClanId() {
    return clanId;
  }

  public void setClanId(int clan) {
    this.clanId = clan;
  }

  @ManyToOne
  @JoinColumn(name = "clan_id")
  @ComputedRelationship
  public Clan getClan() {
    return clan;
  }

  public void setClan(Clan clan) {
    this.clan = clan;
  }

  @ManyToOne
  @JoinColumn(name = "player_id")
  @ComputedRelationship
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  @Id
  @Column(name = "player_id", insertable = false, updatable = false)
  @Exclude
  public int getPlayerId() {
    return playerId;
  }

  public void setPlayerId(int player) {
    this.playerId = player;
  }

  @Basic
  @Column(name = "join_clan_date")
  public Timestamp getJoinClanDate() {
    return joinClanDate;
  }

  public void setJoinClanDate(Timestamp joinClanDate) {
    this.joinClanDate = joinClanDate;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clanId, playerId, joinClanDate);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClanMembership that = (ClanMembership) o;
    return clanId == that.clanId &&
        playerId == that.playerId &&
        Objects.equals(joinClanDate, that.joinClanDate);
  }
}
