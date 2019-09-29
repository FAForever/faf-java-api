package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.CoopMapEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Getter;
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
@Getter
@Setter
public class CoopMap {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "type")
  @Enumerated(EnumType.ORDINAL)
  private MissionType category;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "version")
  private Integer version;

  @Column(name = "filename")
  @Exclude
  private String filename;

  // Set by CoopMapEnhancer
  @Transient
  @ComputedAttribute
  private String downloadUrl;

  @Transient
  @ComputedAttribute
  private String thumbnailUrlLarge;

  @Transient
  @ComputedAttribute
  private String thumbnailUrlSmall;

  @Transient
  @ComputedAttribute
  private String folderName;

  private enum MissionType {
    FA, AEON, CYBRAN, UEF, CUSTOM
  }
}
