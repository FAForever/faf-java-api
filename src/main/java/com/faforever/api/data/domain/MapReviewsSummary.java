package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
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

import static com.faforever.api.data.domain.MapReviewsSummary.TYPE_NAME;

@Entity
@Data
@NoArgsConstructor
@Table(name = "map_reviews_summary")
@Include(name = TYPE_NAME, rootLevel = false)
@Immutable
public class MapReviewsSummary {

  public static final String TYPE_NAME = "mapReviewsSummary";

  @Id
  @Column(name = "id")
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
  @Nullable
  private Float lowerBound;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_id", insertable = false, updatable = false)
  @BatchSize(size = 1000)
  private Map map;
}
