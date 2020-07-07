package com.faforever.api.leaderboard;

import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Entity
@Table(name = "ladder1v1_rating")
public class Ladder1v1LeaderboardEntry {
  private int id;
  private String playerName;
  private Float mean;
  private Float deviation;
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
  public Float getMean() {
    return mean;
  }

  @Column(name = "deviation")
  public Float getDeviation() {
    return deviation;
  }

  @Column(name = "num_games")
  public short getNumGames() {
    return numGames;
  }

  @Column(name = "win_games")
  public short getWonGames() {
    return wonGames;
  }

  @Column(name = "rank")
  public int getRank() {
    return rank;
  }
}
