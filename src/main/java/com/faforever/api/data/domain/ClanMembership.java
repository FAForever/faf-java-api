package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "clan_membership")
@Include(rootLevel = true, type = "clan_membership")
@Data
public class ClanMembership {

  private int id;
  private Timestamp createTime;
  private Timestamp updateTime;
  private Clan clan;
  private Player player;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "clan_id")
  public Clan getClan() {
    return clan;
  }

  @ManyToOne
  @JoinColumn(name = "player_id")
  public Player getPlayer() {
    return player;
  }

  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }
}
