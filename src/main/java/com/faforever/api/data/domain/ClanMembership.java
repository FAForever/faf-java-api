package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "clan_membership")
@Include(rootLevel = true, type = "clanMembership")
@DeletePermission(expression = IsClanMembershipDeletable.EXPRESSION)
@UpdatePermission(expression = IsClanMembershipDeletable.EXPRESSION + " or " + Prefab.UPDATE_ON_CREATE)
@Setter
public class ClanMembership extends AbstractEntity {

  private Clan clan;
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clan_id")
  public Clan getClan() {
    return clan;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  public Player getPlayer() {
    return player;
  }
}
