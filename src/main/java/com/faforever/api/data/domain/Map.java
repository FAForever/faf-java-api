package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.MapChangeListener;
import com.faforever.api.security.elide.permission.AdminMapCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
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
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "map")
@Include(name = Map.TYPE_NAME)
@EntityListeners(MapChangeListener.class)
public class Map implements DefaultEntity, OwnableEntity {

  public static final String TYPE_NAME = "map";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "recommended")
  @Audit(action = Audit.Action.UPDATE, logStatement = "Updated map `{0}` attribute recommended to: {1}", logExpressions = {"${map.id}", "${map.recommended}"})
  @NotNull
  @UpdatePermission(expression = AdminMapCheck.EXPRESSION)
  private boolean recommended;

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

  @Column(name = "games_played")
  @NotNull
  private Integer gamesPlayed;

  @OneToOne(mappedBy = "map")
  @UpdatePermission(expression = Prefab.ALL)
  private MapReviewsSummary reviewsSummary;

  @Transient
  @Override
  public Login getEntityOwner() {
    return author;
  }
}
