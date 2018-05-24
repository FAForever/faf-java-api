package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.MapChangeListener;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.validator.constraints.NotEmpty;
import org.jetbrains.annotations.Nullable;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Table(name = "map")
@Include(rootLevel = true, type = Map.TYPE_NAME)
@Immutable
@EntityListeners(MapChangeListener.class)
public class Map {

  public static final String TYPE_NAME = "map";

  private int id;
  private String displayName;
  private String mapType;
  private String battleType;
  private OffsetDateTime updateTime;
  private OffsetDateTime createTime;
  private List<MapVersion> versions = new ArrayList<>();
  private Player author;
  private MapStatistics statistics;
  private MapVersion latestVersion;
  private int numberOfReviews;
  private float averageReviewScore;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
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

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "reviews")
  public int getNumberOfReviews() {
    return numberOfReviews;
  }

  @Column(name = "average_review_score")
  public float getAverageReviewScore() {
    return averageReviewScore;
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

  @Formula(value = "(SELECT MAX(map_version.update_time) FROM map_version WHERE map_version.map_id = id)")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }
}
