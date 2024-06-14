package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Setter
@Table(name = "game_reviews_summary")
@Include(name = "gameReviewsSummary")
@Immutable
public class GameReviewsSummary {
  private int id;
  private float positive;
  private float negative;
  private float score;
  private int reviews;
  private float lowerBound;
  @Nullable
  private Float averageScore;
  private Game game;

  @Id
  @Column(name = "game_id")
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
  public float getLowerBound() {
    return lowerBound;
  }

  @Column(name = "average_score")
  public Float getAverageScore() {
    return averageScore;
  }

  @OneToOne(mappedBy = "reviewsSummary")
  public Game getGame() {
    return game;
  }
}
