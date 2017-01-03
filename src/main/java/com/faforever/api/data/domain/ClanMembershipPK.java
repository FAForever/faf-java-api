package com.faforever.api.data.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class ClanMembershipPK implements Serializable {

  private int clanId;
  private int playerId;

  @Column(name = "clan_id", insertable = false, updatable = false)
  @Id
  public int getClanId() {
    return clanId;
  }

  public void setClanId(int clanId) {
    this.clanId = clanId;
  }

  @Column(name = "player_id", insertable = false, updatable = false)
  @Id
  public int getPlayerId() {
    return playerId;
  }

  public void setPlayerId(int playerId) {
    this.playerId = playerId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clanId, playerId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClanMembershipPK that = (ClanMembershipPK) o;
    return clanId == that.clanId &&
        playerId == that.playerId;
  }
}
