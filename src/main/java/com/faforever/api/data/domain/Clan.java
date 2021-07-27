package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.ClanEnricherListener;
import com.faforever.api.data.validation.IsLeaderInClan;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "clan")
@Include(name = Clan.TYPE_NAME)
@DeletePermission(expression = IsEntityOwner.EXPRESSION)
@CreatePermission(expression = Prefab.ALL)
@Data
@NoArgsConstructor
@IsLeaderInClan
@EntityListeners(ClanEnricherListener.class)
public class Clan implements DefaultEntity, OwnableEntity {

  public static final String TYPE_NAME = "clan";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "name")
  @NotNull
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private String name;

  @Column(name = "tag")
  @Size(max = 3)
  @NotNull
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private String tag;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "founder_id")
  private Player founder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leader_id")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private Player leader;

  @Column(name = "description")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private String description;

  @Column(name = "tag_color")
  private String tagColor;

  @Transient
  @ComputedAttribute
  private String websiteUrl;

  // Cascading is needed for Create & Delete
  @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, orphanRemoval = true)
  // Permission is managed by ClanMembership class
  @UpdatePermission(expression = Prefab.ALL)
  @NotEmpty(message = "At least the leader should be in the clan")
  @BatchSize(size = 1000)
  private Set<ClanMembership> memberships;

  @Column(name = "requires_invitation", nullable = false)
  private Boolean requiresInvitation;

  @Override
  @Transient
  public Login getEntityOwner() {
    return getLeader();
  }
}
