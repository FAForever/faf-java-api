package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Setter
@Table(name = "map_version_statistics")
@Include(name = "mapVersionStatistics")
@Immutable
public class MapVersionStatistics {
  private Integer id;
  private Integer downloads;
  private Integer plays;
  private Integer draws;
  private MapVersion mapVersion;

  @Id
  @Column(name = "map_version_id")
  public Integer getId() {
    return id;
  }

  @Column(name = "downloads")
  public Integer getDownloads() {
    return downloads;
  }

  @Column(name = "plays")
  public Integer getPlays() {
    return plays;
  }

  @Column(name = "draws")
  public Integer getDraws() {
    return draws;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id", insertable = false, updatable = false)
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}
