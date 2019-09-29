package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import lombok.Getter;
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
@Getter
@Setter
public class ClanMembership extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clan_id")
  private Clan clan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;
}
