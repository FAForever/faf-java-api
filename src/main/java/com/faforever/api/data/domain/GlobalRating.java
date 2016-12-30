package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "global_rating")
@Include(rootLevel = true, type = "global_rating")
public class GlobalRating {

  private int id;
  private Double mean;
  private Double deviation;
  private short numGames;
  private boolean isActive;
  private Player player;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Basic
  @Column(name = "mean")
  public Double getMean() {
    return mean;
  }

  public void setMean(Double mean) {
    this.mean = mean;
  }

  @Basic
  @Column(name = "deviation")
  public Double getDeviation() {
    return deviation;
  }

  public void setDeviation(Double deviation) {
    this.deviation = deviation;
  }

  @Basic
  @Column(name = "numGames")
  public short getNumGames() {
    return numGames;
  }

  public void setNumGames(short numGames) {
    this.numGames = numGames;
  }

  @Basic
  @Column(name = "is_active")
  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  @OneToOne
  @JoinColumn(name = "id", updatable = false, insertable = false)
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, mean, deviation, numGames, isActive);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GlobalRating that = (GlobalRating) o;
    return numGames == that.numGames &&
        isActive == that.isActive &&
        Objects.equals(id, that.id) &&
        Objects.equals(mean, that.mean) &&
        Objects.equals(deviation, that.deviation);
  }
}
