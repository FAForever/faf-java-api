package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.MapChangeListener;
import com.faforever.api.security.elide.permission.AdminMapCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.jetbrains.annotations.Nullable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Table(name = "map")
@Include(name = Map.TYPE_NAME)
@EntityListeners(MapChangeListener.class)
public class Map extends AbstractEntity<Map> implements OwnableEntity {

  public static final String TYPE_NAME = "map";

  private boolean recommended;
  private String displayName;
  private String mapType;
  private String battleType;
  private List<MapVersion> versions = new ArrayList<>();
  private Player author;
  private MapStatistics statistics;
  private MapVersion latestVersion;
  private Integer gamesPlayed;
  private MapReviewsSummary reviewsSummary;
  private License license;

  @Column(name = "recommended")
  @Audit(action = Audit.Action.UPDATE, logStatement = "Updated map `{0}` attribute recommended to: {1}", logExpressions = {"${map.id}", "${map.recommended}"})
  @NotNull
  @UpdatePermission(expression = AdminMapCheck.EXPRESSION)
  public boolean getRecommended() {
    return recommended;
  }

  @Column(name = "display_name", unique = true)
  @Size(max = 100)
  @NotNull
  public String getDisplayName() {
    return displayName;
  }

  @Column(name = "map_type")
  @NotNull
  public String getMapType() {
    return mapType;
  }

  @Column(name = "battle_type")
  @NotNull
  public String getBattleType() {
    return battleType;
  }

  @Column(name = "games_played")
  @NotNull
  public Integer getGamesPlayed() {
    return gamesPlayed;
  }

  @OneToMany(mappedBy = "map", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @NotEmpty
  @Valid
  @BatchSize(size = 1000)
  public List<MapVersion> getVersions() {
    return versions;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author")
  @Nullable
  public Player getAuthor() {
    return author;
  }

  @OneToOne(mappedBy = "map")
  public MapStatistics getStatistics() {
    return statistics;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumnsOrFormulas({
      @JoinColumnOrFormula(
        formula = @JoinFormula(
          value = "(SELECT map_version.id FROM map_version WHERE map_version.map_id = id ORDER BY map_version.version DESC LIMIT 1)",
          referencedColumnName = "id")
      )
  })
  public MapVersion getLatestVersion() {
    return latestVersion;
  }

  @OneToOne(mappedBy = "map")
  @UpdatePermission(expression = Prefab.ALL)
  public MapReviewsSummary getReviewsSummary() {
    return reviewsSummary;
  }

  /**
   * A license is defined on map level (not map version). It is possible to change the license to a license
   * that gives more freedom, but never to a license that reduce given freedoms. This ensures that it doesn't matter
   * when you downloaded the asset, you never lose any usage rights you were already given.
   * If an author wants to "downgrade" a license he needs to create a new map with new versions.
   */
  @ManyToOne
  @JoinColumn(name = "license")
  @Nullable
  public License getLicense() {
    return license;
  }

  @Transient
  @Override
  public Login getEntityOwner() {
    return author;
  }
}
