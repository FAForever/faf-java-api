package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.faforever.api.data.listeners.ClanMembershipChangeListener;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;

import jakarta.persistence.EntityListeners;

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
@EntityListeners(ClanMembershipChangeListener.class)
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
