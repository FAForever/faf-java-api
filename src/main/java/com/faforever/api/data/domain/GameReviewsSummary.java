package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static com.faforever.api.data.domain.GameReviewsSummary.TYPE_NAME;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "game_reviews_summary")
@Include(name = TYPE_NAME)
@Immutable
public class GameReviewsSummary {

  public static final String TYPE_NAME = "gameReview";

  @Id
  @Column(name = "game_id")
  @EqualsAndHashCode.Include
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

  @OneToOne(mappedBy = "reviewsSummary")
  @BatchSize(size = 1000)
  private Game game;
}
