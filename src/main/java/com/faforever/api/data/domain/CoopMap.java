package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.CoopMapEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import static com.faforever.api.data.domain.CoopMap.TYPE_NAME;

@Entity
@Table(name = "coop_map")
@EntityListeners(CoopMapEnricher.class)
@Include(name = TYPE_NAME)
@Setter
public class CoopMap {
  public static final String TYPE_NAME = "coopMission";

  private int id;
  private Integer order;
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
  private CoopScenario scenario;

  @Deprecated(forRemoval = true)
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

  @Column(name = "order")
  public Integer getOrder() {
    return order;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scenario_id")
  public CoopScenario getScenario() {
    return this.scenario;
  }

  private enum MissionType {
    FA, AEON, CYBRAN, UEF, CUSTOM
  }
}
