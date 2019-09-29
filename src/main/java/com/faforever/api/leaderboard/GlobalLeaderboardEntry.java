package com.faforever.api.leaderboard;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "global_rating")
@Immutable
public class GlobalLeaderboardEntry {

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

  @Column(name = "rank")
  private int rank;
}
