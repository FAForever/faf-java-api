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
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

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
@Setter
@EntityListeners(MapVersionEnricher.class)
@Table(name = "map_version")
@Include(rootLevel = true, type = MapVersion.TYPE_NAME)
@Indexed
public class MapVersion extends AbstractEntity implements OwnableEntity {

  public static final String TYPE_NAME = "mapVersion";

  private String description;
  private int maxPlayers;
  private int width;
  private int height;
  private int version;
  private String filename;
  private String folderName;
  private boolean ranked;
  private boolean hidden;
  private Map map;
  private MapVersionStatistics statistics;
  private String thumbnailUrlSmall;
  private String thumbnailUrlLarge;
  private String downloadUrl;
  private List<MapVersionReview> reviews;
  private MapVersionReviewsSummary reviewsSummary;
  private Ladder1v1Map ladder1v1Map;

  @UpdatePermission(expression = IsEntityOwner.EXPRESSION + " or " + AdminMapCheck.EXPRESSION)
  @Column(name = "description")
  @Field(index = Index.YES, analyze = Analyze.YES,
    store = Store.NO, analyzer = @Analyzer(definition = "case_insensitive"))
  public String getDescription() {
    return description;
  }

  @Column(name = "max_players")
  @NotNull
  public int getMaxPlayers() {
    return maxPlayers;
  }

  @Column(name = "width")
  // FIXME: validation
  public int getWidth() {
    return width;
  }

  @Column(name = "height")
  // FIXME: validation
  public int getHeight() {
    return height;
  }

  @Column(name = "version")
  // FIXME: validation
  public int getVersion() {
    return version;
  }

  @Column(name = "filename")
  @NotNull
  public String getFilename() {
    return filename;
  }

  @UpdatePermission(expression = AdminMapCheck.EXPRESSION + " or (" + IsEntityOwner.EXPRESSION + " and " + BooleanChange.TO_FALSE_EXPRESSION + ")")
  @Audit(action = Action.UPDATE, logStatement = "Updated map version `{0}` attribute ranked to: {1}", logExpressions = {"${mapVersion.id}", "${mapVersion.ranked}"})
  @Column(name = "ranked")
  public boolean isRanked() {
    return ranked;
  }

  @UpdatePermission(expression = AdminMapCheck.EXPRESSION + " or (" + IsEntityOwner.EXPRESSION + " and " + BooleanChange.TO_TRUE_EXPRESSION + ")")
  @Audit(action = Action.UPDATE, logStatement = "Updated map version `{0}` attribute hidden to: {1}", logExpressions = {"${mapVersion.id}", "${mapVersion.hidden}"})
  @Column(name = "hidden")
  public boolean isHidden() {
    return hidden;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_id")
  @NotNull
  @BatchSize(size = 1000)
  public Map getMap() {
    return this.map;
  }

  @OneToOne(mappedBy = "mapVersion", fetch = FetchType.EAGER)
  public MapVersionStatistics getStatistics() {
    return statistics;
  }

  @Transient
  @ComputedAttribute
  public String getThumbnailUrlSmall() {
    return thumbnailUrlSmall;
  }

  @Transient
  @ComputedAttribute
  public String getThumbnailUrlLarge() {
    return thumbnailUrlLarge;
  }

  @Transient
  @ComputedAttribute
  public String getDownloadUrl() {
    return downloadUrl;
  }

  @Transient
  @ComputedAttribute
  public String getFolderName() {
    return folderName;
  }

  @OneToMany(mappedBy = "mapVersion")
  @UpdatePermission(expression = Prefab.ALL)
  public List<MapVersionReview> getReviews() {
    return reviews;
  }

  @OneToOne(mappedBy = "mapVersion")
  @UpdatePermission(expression = Prefab.ALL)
  public MapVersionReviewsSummary getReviewsSummary() {
    return reviewsSummary;
  }

  @Transient
  @Override
  public Login getEntityOwner() {
    return map.getEntityOwner();
  }
}
