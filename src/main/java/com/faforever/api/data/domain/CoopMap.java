package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.CoopMapEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "coop_map")
@EntityListeners(CoopMapEnricher.class)
@Include(rootLevel = true, type = "coopMission")
@Setter
public class CoopMap {

  private int id;
  private MissionType category;
  private String name;
  private String description;
  private Integer version;
  private String filename;
  // Set by CoopMapEnhancer
  private String downloadUrl;
  private String thumbnailUrlLarge;
  private String thumbnailUrlSmall;
  private String folderName;

  @Column(name = "type")
  @Enumerated(EnumType.ORDINAL)
  public MissionType getCategory() {
    return category;
  }

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "name")
  public String getName() {
    return name;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Column(name = "version")
  public Integer getVersion() {
    return version;
  }

  @Column(name = "filename")
  @Exclude
  public String getFilename() {
    return filename;
  }

  @Transient
  @ComputedAttribute
  public String getDownloadUrl() {
    return downloadUrl;
  }

  @Transient
  @ComputedAttribute
  public String getThumbnailUrlLarge() {
    return thumbnailUrlLarge;
  }

  @Transient
  @ComputedAttribute
  public String getThumbnailUrlSmall() {
    return thumbnailUrlSmall;
  }

  @Transient
  @ComputedAttribute
  public String getFolderName() {
    return folderName;
  }

  private enum MissionType {
    FA, AEON, CYBRAN, UEF, CUSTOM
  }
}
