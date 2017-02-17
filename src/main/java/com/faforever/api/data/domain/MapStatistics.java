package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "map_statistics")
@Include(rootLevel = true, type = "mapStatistics")
@Immutable
public class MapStatistics {
  private int id;
  private int downloads;
  private int plays;
  private int draws;
  private Map map;

  @Id
  @Column(name = "map_id")
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

  @OneToOne
  @JoinColumn(name = "map_id", insertable = false, updatable = false)
  public Map getMap() {
    return map;
  }
}
