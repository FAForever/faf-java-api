package com.faforever.api.data.domain;


import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Setter
public abstract class RatingWithRank {
  private int id;
  private Double mean;
  private Double deviation;
  private Player player;
  private double rating;
  private int numberOfGames;
  private int rank;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "mean")
  public Double getMean() {
    return mean;
  }

  @Column(name = "deviation")
  public Double getDeviation() {
    return deviation;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id", updatable = false, insertable = false)
  public Player getPlayer() {
    return player;
  }

  @Column(name = "rating", updatable = false)
  @Generated(GenerationTime.ALWAYS)
  public double getRating() {
    return rating;
  }

  @Column(name = "num_games")
  public int getNumberOfGames() {
    return numberOfGames;
  }

  @Column(name = "ranking", updatable = false)
  public int getRank() {
    return rank;
  }

}
