package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.ClanChangeListener;
import com.faforever.api.data.listeners.ClanEnricherListener;
import com.faforever.api.data.validation.IsLeaderInClan;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "clan")
@Include(name = Clan.TYPE_NAME)
@DeletePermission(expression = IsEntityOwner.EXPRESSION)
@CreatePermission(expression = Prefab.ALL)
@Setter
@IsLeaderInClan
@EntityListeners({ClanEnricherListener.class, ClanChangeListener.class})
public class Clan extends AbstractEntity<Clan> implements OwnableEntity {

  public static final String TYPE_NAME = "clan";

  private String name;
  private String tag;
  private Player founder;
  private Player leader;
  private String description;
  private String tagColor;
  private String websiteUrl;
  private Set<ClanMembership> memberships;
  private Boolean requiresInvitation;

  @Column(name = "name")
  @NotNull
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  public String getName() {
    return name;
  }

  @Column(name = "tag")
  @Size(max = 3)
  @NotNull
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  public String getTag() {
    return tag;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "founder_id")
  public Player getFounder() {
    return founder;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leader_id")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  public Player getLeader() {
    return leader;
  }

  @Column(name = "description")
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  public String getDescription() {
    return description;
  }

  @Column(name = "tag_color")
  public String getTagColor() {
    return tagColor;
  }

  // Cascading is needed for Create & Delete
  @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, orphanRemoval = true)
  // Permission is managed by ClanMembership class
  @UpdatePermission(expression = Prefab.ALL)
  @NotEmpty(message = "At least the leader should be in the clan")
  @BatchSize(size = 1000)
  public Set<ClanMembership> getMemberships() {
    return this.memberships;
  }

  @Column(name = "requires_invitation", nullable = false)
  public Boolean getRequiresInvitation() {
    return requiresInvitation;
  }

  @Transient
  @ComputedAttribute
  public String getWebsiteUrl() {
    return websiteUrl;
  }

  @Override
  @Transient
  public Login getEntityOwner() {
    return getLeader();
  }
}
