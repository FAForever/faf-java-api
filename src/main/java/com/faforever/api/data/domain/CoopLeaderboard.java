package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Time;
import java.util.Objects;

@Entity
@Table(name = "coop_leaderboard")
@Include(rootLevel = true, type = "coop_leaderboard")
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

  public void setId(int id) {
    this.id = id;
  }

  @Basic
  @Column(name = "mission")
  public short getMission() {
    return mission;
  }

  public void setMission(short mission) {
    this.mission = mission;
  }

  @OneToOne
  @JoinColumn(name = "gameuid")
  public Replay getReplay() {
    return replay;
  }

  public void setReplay(Replay gameuid) {
    this.replay = gameuid;
  }

  @Basic
  @Column(name = "secondary")
  public boolean getSecondaryObjectives() {
    return secondaryObjectives;
  }

  public void setSecondaryObjectives(boolean secondary) {
    this.secondaryObjectives = secondary;
  }

  @Basic
  @Column(name = "time")
  public Time getDuration() {
    return duration;
  }

  public void setDuration(Time time) {
    this.duration = time;
  }

  @Basic
  @Column(name = "player_count")
  public short getPlayerCount() {
    return playerCount;
  }

  public void setPlayerCount(short playerCount) {
    this.playerCount = playerCount;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, mission, replay, secondaryObjectives, duration, playerCount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoopLeaderboard that = (CoopLeaderboard) o;
    return id == that.id &&
        mission == that.mission &&
        replay == that.replay &&
        secondaryObjectives == that.secondaryObjectives &&
        Objects.equals(duration, that.duration) &&
        Objects.equals(playerCount, that.playerCount);
  }
}
