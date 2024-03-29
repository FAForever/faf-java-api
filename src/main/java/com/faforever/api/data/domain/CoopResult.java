package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "coop_leaderboard")
@Include(name = CoopResult.TYPE_NAME)
@Setter
public class CoopResult {

  public static final String TYPE_NAME = "coopResult";

  private int id;
  private short mission;
  private Game game;
  private boolean secondaryObjectives;
  private long duration;
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
  public Game getGame() {
    return game;
  }

  @Column(name = "secondary")
  public boolean getSecondaryObjectives() {
    return secondaryObjectives;
  }

  @Column(name = "time")
  @Convert(converter = TimeConverter.class)
  public long getDuration() {
    return duration;
  }

  @Column(name = "player_count")
  public short getPlayerCount() {
    return playerCount;
  }
}
