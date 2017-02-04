package com.faforever.api.leaderboard;

import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Entity
@Table(name = "ladder1v1_rating")
public class Ranked1v1LeaderboardEntry {
  private int id;
  private String playerName;
  private Double mean;
  private Double deviation;
  private short numGames;
  private short wonGames;
  private int rank;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "login")
  public String getPlayerName() {
    return playerName;
  }

  @Column(name = "mean")
  public Double getMean() {
    return mean;
  }

  @Column(name = "deviation")
  public Double getDeviation() {
    return deviation;
  }

  @Column(name = "numGames")
  public short getNumGames() {
    return numGames;
  }

  @Column(name = "winGames")
  public short getWonGames() {
    return wonGames;
  }

  @Column(name = "rank")
  public int getRank() {
    return rank;
  }
}
