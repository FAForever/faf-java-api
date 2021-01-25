package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.MapChangeListener;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Table(name = "map")
@Include(rootLevel = true, type = Map.TYPE_NAME)
@Immutable
@EntityListeners(MapChangeListener.class)
@Indexed
public class Map extends AbstractEntity implements OwnableEntity {

  public static final String TYPE_NAME = "map";

  private String displayName;
  private String mapType;
  private String battleType;
  private List<MapVersion> versions = new ArrayList<>();
  private Player author;
  private MapStatistics statistics;
  private MapVersion latestVersion;
  private MapReviewsSummary reviewsSummary;

  @Column(name = "display_name", unique = true)
  @Size(max = 100)
  @NotNull
  @Field(index = Index.YES, analyze = Analyze.YES,
    store = Store.NO, analyzer = @Analyzer(definition = "case_insensitive"))
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
  @BatchSize(size = 1000)
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
  @BatchSize(size = 1000)
  public MapVersion getLatestVersion() {
    return latestVersion;
  }

  @OneToOne(mappedBy = "map")
  @UpdatePermission(expression = Prefab.ALL)
  public MapReviewsSummary getReviewsSummary() {
    return reviewsSummary;
  }

  @Transient
  @Override
  public Login getEntityOwner() {
    return author;
  }
}
