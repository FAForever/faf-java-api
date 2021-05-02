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
  private int id;
  private int downloads;
  private int plays;
  private int draws;
  private MapVersion mapVersion;

  @Id
  @Column(name = "map_version_id")
  public int getId() {
    return id;
  }

  @Column(name = "downloads")
  public int getDownloads() {
    return downloads;
  }

  @Column(name = "plays")
  public int getPlays() {
    return plays;
  }

  @Column(name = "draws")
  public int getDraws() {
    return draws;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id", insertable = false, updatable = false)
  @BatchSize(size = 1000)
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}
