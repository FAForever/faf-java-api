package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.MapVersionEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

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
@Include(rootLevel = true, type = "mapVersion")
public class MapVersion extends AbstractEntity {

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

  @Column(name = "description")
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

  @Column(name = "ranked")
  public boolean isRanked() {
    return ranked;
  }

  @Column(name = "hidden")
  public boolean isHidden() {
    return hidden;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_id")
  @NotNull
  public Map getMap() {
    return this.map;
  }

  @OneToOne(mappedBy = "mapVersion", fetch = FetchType.LAZY)
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
  @UpdatePermission(expression = "Prefab.Role.All")
  public List<MapVersionReview> getReviews() {
    return reviews;
  }
}
