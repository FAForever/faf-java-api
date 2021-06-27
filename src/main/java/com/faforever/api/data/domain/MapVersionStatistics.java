package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static com.faforever.api.data.domain.MapVersionStatistics.TYPE_NAME;

@Entity
@Data
@NoArgsConstructor
@Table(name = "map_version_statistics")
@Include(name = TYPE_NAME)
@Immutable
public class MapVersionStatistics {

  public static final String TYPE_NAME = "mapVersionStatistics";

  @Id
  @Column(name = "map_version_id")
  private Integer id;

  @Column(name = "downloads")
  private Integer downloads;

  @Column(name = "plays")
  private Integer plays;

  @Column(name = "draws")
  private Integer draws;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id", insertable = false, updatable = false)
  @BatchSize(size = 1000)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private MapVersion mapVersion;
}
