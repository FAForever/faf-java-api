package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lobby_ban")
@Include(rootLevel = true, type = "ban_details")
public class BanDetails {

  private int id;
  private Player player;
  private String reason;
  private Timestamp expiresAt;

  @Id
  @Column(name = "idUser")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @OneToOne
  @JoinColumn(name = "idUser", updatable = false)
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  @Basic
  @Column(name = "reason")
  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  @Basic
  @Column(name = "expires_at")
  public Timestamp getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Timestamp expiresAt) {
    this.expiresAt = expiresAt;
  }

  @Override
  public int hashCode() {
    return Objects.hash(player, reason, expiresAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BanDetails banDetails = (BanDetails) o;
    return player == banDetails.player &&
        Objects.equals(reason, banDetails.reason) &&
        Objects.equals(expiresAt, banDetails.expiresAt);
  }
}
