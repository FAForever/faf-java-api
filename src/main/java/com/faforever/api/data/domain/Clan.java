package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.listeners.ClanEnricherListener;
import com.faforever.api.data.validation.IsLeaderInClan;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "clan")
@Include(rootLevel = true, type = Clan.TYPE_NAME)
@SharePermission
@DeletePermission(expression = IsEntityOwner.EXPRESSION)
@CreatePermission(expression = "Prefab.Role.All")
@Getter
@Setter
@IsLeaderInClan
@EntityListeners(ClanEnricherListener.class)
public class Clan extends AbstractEntity implements OwnableEntity {

  public static final String TYPE_NAME = "clan";

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
  @UpdatePermission(expression = "Prefab.Role.All")
  @NotEmpty(message = "At least the leader should be in the clan")
  private List<ClanMembership> memberships;

  @Override
  @Transient
  public Login getEntityOwner() {
    return getLeader();
  }
}
