package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "game_reviews_summary")
@Include(type = "gameReviewsSummary")
@Immutable
public class GameReviewsSummary {
  private int id;
  private float positive;
  private float negative;
  private float score;
  private int reviews;
  private float lowerBound;
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

  @OneToOne(mappedBy = "reviewsSummary")
  @BatchSize(size = 1000)
  public Game getGame() {
    return game;
  }
}
