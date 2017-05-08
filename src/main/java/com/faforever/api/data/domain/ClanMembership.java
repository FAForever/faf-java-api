package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "clan_membership")
@Include(rootLevel = true, type = "clanMembership")
@DeletePermission(expression = IsClanMembershipDeletable.EXPRESSION)
@Setter
public class ClanMembership extends AbstractEntity {

  private Clan clan;
  private Player player;

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
}
