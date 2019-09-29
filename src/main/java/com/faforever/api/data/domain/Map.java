package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.MapChangeListener;
import com.yahoo.elide.annotation.Include;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "map")
@Include(rootLevel = true, type = Map.TYPE_NAME)
@Immutable
@EntityListeners(MapChangeListener.class)
public class Map {

  public static final String TYPE_NAME = "map";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "display_name", unique = true)
  @Size(max = 100)
  @NotNull
  private String displayName;

  @Column(name = "map_type")
  @NotNull
  private String mapType;

  @Column(name = "battle_type")
  @NotNull
  private String battleType;

  @Formula(value = "(SELECT MAX(map_version.update_time) FROM map_version WHERE map_version.map_id = id)")
  private OffsetDateTime updateTime;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @OneToMany(mappedBy = "map", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @NotEmpty
  @Valid
  @BatchSize(size = 1000)
  private List<MapVersion> versions = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author")
  @Nullable
  @BatchSize(size = 1000)
  private Player author;

  @OneToOne(mappedBy = "map")
  private MapStatistics statistics;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(
      formula = @JoinFormula(
        value = "(SELECT map_version.id FROM map_version WHERE map_version.map_id = id ORDER BY map_version.version DESC LIMIT 1)",
        referencedColumnName = "id")
    )
  })
  @BatchSize(size = 1000)
  private MapVersion latestVersion;

  @Column(name = "reviews")
  private int numberOfReviews;

  @Column(name = "average_review_score")
  private float averageReviewScore;
}
