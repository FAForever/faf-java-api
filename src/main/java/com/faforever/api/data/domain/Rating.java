package com.faforever.api.data.domain;


import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Setter
public abstract class Rating {

  private int id;
  private Double mean;
  private Double deviation;
  private Player player;
  private double rating;

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
  public double getRating() {
    return rating;
  }
}
