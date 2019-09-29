package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Getter;
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
@Getter
@Setter
@Table(name = "map_version_reviews_summary")
@Include(type = "mapVersionReviewsSummary")
@Immutable
public class MapVersionReviewsSummary {

  @Id
  @Column(name = "map_version_id")
  private int id;

  @Column(name = "positive")
  private float positive;

  @Column(name = "negative")
  private float negative;

  @Column(name = "score")
  private float score;

  @Column(name = "reviews")
  private int reviews;

  @Column(name = "lower_bound")
  private float lowerBound;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id", insertable = false, updatable = false)
  @BatchSize(size = 1000)
  private MapVersion mapVersion;
}
