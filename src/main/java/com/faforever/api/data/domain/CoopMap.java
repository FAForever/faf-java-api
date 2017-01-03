package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.CoopMapEnhancer;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Objects;

@Entity
@Table(name = "coop_map")
@EntityListeners(CoopMapEnhancer.class)
@Include(rootLevel = true, type = "coop_mission")
public class CoopMap {

  private enum MissionType {
    FA, AEON, CYBRAN, UEF, CUSTOM
  }

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

  @Basic
  @Column(name = "type")
  @Enumerated(EnumType.ORDINAL)
  public MissionType getCategory() {
    return category;
  }

  public void setCategory(MissionType type) {
    this.category = type;
  }

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Basic
  @Column(name = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Basic
  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Basic
  @Column(name = "version")
  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  @Basic
  @Column(name = "filename")
  @Exclude
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Transient
  @ComputedAttribute
  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  @Transient
  @ComputedAttribute
  public String getThumbnailUrlLarge() {
    return thumbnailUrlLarge;
  }

  public void setThumbnailUrlLarge(String thumbnailUrlLarge) {
    this.thumbnailUrlLarge = thumbnailUrlLarge;
  }

  @Transient
  @ComputedAttribute
  public String getThumbnailUrlSmall() {
    return thumbnailUrlSmall;
  }

  public void setThumbnailUrlSmall(String thumbnailUrlSmall) {
    this.thumbnailUrlSmall = thumbnailUrlSmall;
  }

  @Transient
  @ComputedAttribute
  public String getFolderName() {
    return folderName;
  }

  public void setFolderName(String folderName) {
    this.folderName = folderName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, id, name, description, version, filename);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoopMap coopMap = (CoopMap) o;
    return id == coopMap.id &&
        Objects.equals(category, coopMap.category) &&
        Objects.equals(name, coopMap.name) &&
        Objects.equals(description, coopMap.description) &&
        Objects.equals(version, coopMap.version) &&
        Objects.equals(filename, coopMap.filename);
  }
}
