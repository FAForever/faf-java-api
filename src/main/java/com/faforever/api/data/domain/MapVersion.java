package com.faforever.api.data.domain;

import com.faforever.api.data.checks.BooleanChange;
import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.MapVersionEnricher;
import com.faforever.api.security.elide.permission.AdminMapCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

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
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EntityListeners(MapVersionEnricher.class)
@Table(name = "map_version")
@Include(name = MapVersion.TYPE_NAME)
public class MapVersion implements DefaultEntity, OwnableEntity {

  public static final String TYPE_NAME = "mapVersion";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @UpdatePermission(expression = IsEntityOwner.EXPRESSION + " or " + AdminMapCheck.EXPRESSION)
  @Column(name = "description")
  private String description;

  @Column(name = "max_players")
  @NotNull
  private int maxPlayers;

  @Column(name = "width")
  // FIXME: validation
  private int width;

  @Column(name = "height")
  // FIXME: validation
  private int height;

  @Column(name = "version")
  // FIXME: validation
  private int version;

  @Column(name = "filename")
  @NotNull
  private String filename;

  @Transient
  @ComputedAttribute
  private String folderName;

  @UpdatePermission(expression = AdminMapCheck.EXPRESSION + " or (" + IsEntityOwner.EXPRESSION + " and " + BooleanChange.TO_FALSE_EXPRESSION + ")")
  @Audit(action = Action.UPDATE, logStatement = "Updated map version `{0}` attribute ranked to: {1}", logExpressions = {"${mapVersion.id}", "${mapVersion.ranked}"})
  @Column(name = "ranked")
  private boolean ranked;

  @UpdatePermission(expression = AdminMapCheck.EXPRESSION + " or (" + IsEntityOwner.EXPRESSION + " and " + BooleanChange.TO_TRUE_EXPRESSION + ")")
  @Audit(action = Action.UPDATE, logStatement = "Updated map version `{0}` attribute hidden to: {1}", logExpressions = {"${mapVersion.id}", "${mapVersion.hidden}"})
  @Column(name = "hidden")
  private boolean hidden;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_id")
  @NotNull
  @BatchSize(size = 1000)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Map map;

  @OneToOne(mappedBy = "mapVersion", fetch = FetchType.EAGER)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private MapVersionStatistics statistics;

  @Transient
  @ComputedAttribute
  private String thumbnailUrlSmall;

  @Transient
  @ComputedAttribute
  private String thumbnailUrlLarge;

  @Transient
  @ComputedAttribute
  private String downloadUrl;

  @Column(name = "games_played")
  @NotNull
  private Integer gamesPlayed;

  @OneToMany(mappedBy = "mapVersion")
  @UpdatePermission(expression = Prefab.ALL)
  @EqualsAndHashCode.Exclude
  private List<MapVersionReview> reviews;

  @OneToOne(mappedBy = "mapVersion")
  @UpdatePermission(expression = Prefab.ALL)
  @EqualsAndHashCode.Exclude
  private MapVersionReviewsSummary reviewsSummary;

  @Transient
  @Override
  public Login getEntityOwner() {
    return map.getEntityOwner();
  }
}
