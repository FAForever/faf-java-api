package com.faforever.api.leaderboard;

import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Entity
@Table(name = "global_rating")
@Immutable
public class GlobalLeaderboardEntry {
  private int id;
  private String playerName;
  private Float mean;
  private Float deviation;
  private short numGames;
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

  @Column(name = "numGames")
  public short getNumGames() {
    return numGames;
  }

  @Column(name = "rank")
  public int getRank() {
    return rank;
  }
}
