package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Setter
@Table(name = "mod_reviews_summary")
@Include(name = "modReviewsSummary", rootLevel = false)
@Immutable
public class ModReviewsSummary {
  private int id;
  private float positive;
  private float negative;
  private float score;
  private int reviews;
  @Nullable
  private Float lowerBound;
  @Nullable
  private Float averageScore;
  private Mod mod;

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

  @Column(name = "average_score")
  public Float getAverageScore() {
    return averageScore;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mod_id", insertable = false, updatable = false)
  public Mod getMod() {
    return mod;
  }
}
