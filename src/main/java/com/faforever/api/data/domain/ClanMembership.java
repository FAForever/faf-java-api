package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clan_membership")
@Include(name = "clanMembership")
@DeletePermission(expression = IsClanMembershipDeletable.EXPRESSION)
@UpdatePermission(expression = IsClanMembershipDeletable.EXPRESSION)
@Setter
public class ClanMembership extends AbstractEntity<ClanMembership> {

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
