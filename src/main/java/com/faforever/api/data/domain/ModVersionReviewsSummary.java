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

import static com.faforever.api.data.domain.ModVersionReviewsSummary.TYPE_NAME;

@Entity
@Data
@NoArgsConstructor
@Table(name = "mod_version_reviews_summary")
@Include(name = TYPE_NAME, rootLevel = false)
@Immutable
public class ModVersionReviewsSummary {

  public static final String TYPE_NAME = "modVersionReviewsSummary";

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
  @JoinColumn(name = "mod_version_id", insertable = false, updatable = false)
  @BatchSize(size = 1000)
  private ModVersion modVersion;
}
