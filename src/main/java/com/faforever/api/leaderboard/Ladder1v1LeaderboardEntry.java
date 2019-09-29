package com.faforever.api.leaderboard;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "ladder1v1_rating")
public class Ladder1v1LeaderboardEntry {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "login")
  private String playerName;

  @Column(name = "mean")
  private Float mean;

  @Column(name = "deviation")
  private Float deviation;

  @Column(name = "numGames")
  private short numGames;

  @Column(name = "winGames")
  private short wonGames;

  @Column(name = "rank")
  private int rank;
}
