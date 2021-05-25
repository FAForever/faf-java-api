package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "map_version_statistics")
@Include(type = "mapVersionStatistics")
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
  @BatchSize(size = 1000)
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}
