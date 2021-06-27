package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@Table(name = "map_statistics")
@Include(name = MapStatistics.TYPE_NAME)
@Immutable
public class MapStatistics {
  public static final String TYPE_NAME = "mapStatistics";

  @Id
  @Column(name = "map_id")
  private Integer id;

  @Column(name = "downloads")
  private Integer downloads;

  @Column(name = "plays")
  private Integer plays;

  @Column(name = "draws")
  private Integer draws;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_id", insertable = false, updatable = false)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Map map;
}
