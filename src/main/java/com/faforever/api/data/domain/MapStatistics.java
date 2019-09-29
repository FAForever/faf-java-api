package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "map_statistics")
@Include(rootLevel = true, type = MapStatistics.TYPE_NAME)
@Immutable
public class MapStatistics {
  public static final String TYPE_NAME = "mapStatistics";

  @Id
  @Column(name = "map_id")
  private int id;

  @Column(name = "downloads")
  private int downloads;

  @Column(name = "plays")
  private int plays;

  @Column(name = "draws")
  private int draws;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_id", insertable = false, updatable = false)
  private Map map;
}
