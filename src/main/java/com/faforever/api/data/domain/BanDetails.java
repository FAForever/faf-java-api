package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "lobby_ban")
@Include(rootLevel = true, type = "banDetails")
@Setter
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

  @OneToOne
  @JoinColumn(name = "idUser", updatable = false)
  public Player getPlayer() {
    return player;
  }

  @Column(name = "reason")
  public String getReason() {
    return reason;
  }

  @Column(name = "expires_at")
  public Timestamp getExpiresAt() {
    return expiresAt;
  }
}
