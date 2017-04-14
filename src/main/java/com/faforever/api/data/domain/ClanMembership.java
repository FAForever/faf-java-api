package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "clan_membership")
@Include(rootLevel = true, type = "clanMembership")
@DeletePermission(expression = IsClanMembershipDeletable.EXPRESSION)
@Setter
public class ClanMembership {

  private int id;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private Clan clan;
  private Player player;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clan_id")
  public Clan getClan() {
    return clan;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  public Player getPlayer() {
    return player;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }
}
