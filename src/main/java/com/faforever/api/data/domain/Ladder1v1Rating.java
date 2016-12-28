package com.faforever.api.data.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "ladder1v1_rating")
public class Ladder1v1Rating {

  private int id;
  private Double mean;
  private Double deviation;
  private short numGames;
  private short winGames;
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
  @Column(name = "winGames")
  public short getWinGames() {
    return winGames;
  }

  public void setWinGames(short winGames) {
    this.winGames = winGames;
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
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, mean, deviation, numGames, winGames, isActive);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ladder1v1Rating that = (Ladder1v1Rating) o;
    return numGames == that.numGames &&
        winGames == that.winGames &&
        isActive == that.isActive &&
        Objects.equals(id, that.id) &&
        Objects.equals(mean, that.mean) &&
        Objects.equals(deviation, that.deviation);
  }
}
