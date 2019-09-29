package com.faforever.api.data.domain;

import com.faforever.api.data.checks.BooleanChange;
import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.MapVersionEnricher;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

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
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Getter
@Setter
@EntityListeners(MapVersionEnricher.class)
@Table(name = "map_version")
@Include(rootLevel = true, type = MapVersion.TYPE_NAME)
public class MapVersion extends AbstractEntity implements OwnableEntity {

  public static final String TYPE_NAME = "mapVersion";

  @UpdatePermission(expression = IsEntityOwner.EXPRESSION + " or " + IsModerator.EXPRESSION)
  @Column(name = "description")
  private String description;

  @Column(name = "max_players")
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

  @UpdatePermission(expression = IsModerator.EXPRESSION + " or (" + IsEntityOwner.EXPRESSION + " and " + BooleanChange.TO_FALSE_EXPRESSION + ")")
  @Audit(action = Action.UPDATE, logStatement = "Updated map version `{0}` attribute ranked to: {1}", logExpressions = {"${mapVersion.id}", "${mapVersion.ranked}"})
  @Column(name = "ranked")
  private boolean ranked;

  @UpdatePermission(expression = IsModerator.EXPRESSION + " or (" + IsEntityOwner.EXPRESSION + " and " + BooleanChange.TO_TRUE_EXPRESSION + ")")
  @Audit(action = Action.UPDATE, logStatement = "Updated map version `{0}` attribute hidden to: {1}", logExpressions = {"${mapVersion.id}", "${mapVersion.hidden}"})
  @Column(name = "hidden")
  private boolean hidden;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_id")
  @NotNull
  @BatchSize(size = 1000)
  private Map map;

  @OneToOne(mappedBy = "mapVersion", fetch = FetchType.EAGER)
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

  @Transient
  @ComputedAttribute
  private String folderName;

  @OneToMany(mappedBy = "mapVersion")
  @UpdatePermission(expression = "Prefab.Role.All")
  private List<MapVersionReview> reviews;

  @OneToOne(mappedBy = "mapVersion")
  @UpdatePermission(expression = "Prefab.Role.All")
  private MapVersionReviewsSummary reviewsSummary;

  @Transient
  @Override
  public Login getEntityOwner() {
    return map.getAuthor();
  }
}
