package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Time;

@Entity
@Table(name = "coop_leaderboard")
@Include(rootLevel = true, type = "coopLeaderboard")
@Setter
public class CoopLeaderboard {

  private int id;
  private short mission;
  private Replay replay;
  private boolean secondaryObjectives;
  private Time duration;
  private short playerCount;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "mission")
  public short getMission() {
    return mission;
  }

  @OneToOne
  @JoinColumn(name = "gameuid")
  public Replay getReplay() {
    return replay;
  }

  @Column(name = "secondary")
  public boolean getSecondaryObjectives() {
    return secondaryObjectives;
  }

  @Column(name = "time")
  public Time getDuration() {
    return duration;
  }

  @Column(name = "player_count")
  public short getPlayerCount() {
    return playerCount;
  }
}
