package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "map_version_reviews_summary")
@Include(name = "mapVersionReviewsSummary", rootLevel = false)
@Immutable
public class MapVersionReviewsSummary {
  private int id;
  private float positive;
  private float negative;
  private float score;
  private int reviews;
  @Nullable
  private Float lowerBound;
  private MapVersion mapVersion;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "positive")
  public float getPositive() {
    return positive;
  }

  @Column(name = "negative")
  public float getNegative() {
    return negative;
  }

  @Column(name = "score")
  public float getScore() {
    return score;
  }

  @Column(name = "reviews")
  public int getReviews() {
    return reviews;
  }

  @Column(name = "lower_bound")
  @Nullable
  public Float getLowerBound() {
    return lowerBound;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id", insertable = false, updatable = false)
  @BatchSize(size = 1000)
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}
