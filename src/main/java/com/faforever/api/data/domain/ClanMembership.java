package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsClanMembershipDeletable;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

import static com.faforever.api.data.domain.ClanMembership.TYPE_NAME;

@Entity
@Table(name = "clan_membership")
@Include(name = TYPE_NAME)
@DeletePermission(expression = IsClanMembershipDeletable.EXPRESSION)
@UpdatePermission(expression = IsClanMembershipDeletable.EXPRESSION)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Access(AccessType.FIELD)
public class ClanMembership implements DefaultEntity {

  public static final String TYPE_NAME = "clanMembership";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clan_id")
  private Clan clan;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;
}
