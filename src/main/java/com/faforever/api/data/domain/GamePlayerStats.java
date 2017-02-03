package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "game_player_stats")
@Include(rootLevel = true, type = "gamePlayerStats")
@Immutable
@Setter
public class GamePlayerStats {

  private long id;
  private Player player;
  private byte ai;
  private byte faction;
  private byte color;
  private byte team;
  private byte place;
  private Double mean;
  private Double deviation;
  private Double afterMean;
  private Double afterDeviation;
  private byte score;
  private Timestamp scoreTime;
  private Replay replay;

  @Id
  @Column(name = "id")
  public long getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "playerId")
  public Player getPlayer() {
    return player;
  }

  @Column(name = "AI")
  public byte getAi() {
    return ai;
  }

  @Column(name = "faction")
  public byte getFaction() {
    return faction;
  }

  @Column(name = "color")
  public byte getColor() {
    return color;
  }

  @Column(name = "team")
  public byte getTeam() {
    return team;
  }

  @Column(name = "place")
  public byte getPlace() {
    return place;
  }

  @Column(name = "mean")
  public Double getMean() {
    return mean;
  }

  @Column(name = "deviation")
  public Double getDeviation() {
    return deviation;
  }

  @Column(name = "after_mean")
  public Double getAfterMean() {
    return afterMean;
  }

  @Column(name = "after_deviation")
  public Double getAfterDeviation() {
    return afterDeviation;
  }

  @Column(name = "score")
  public byte getScore() {
    return score;
  }

  @Column(name = "scoreTime")
  public Timestamp getScoreTime() {
    return scoreTime;
  }

  @ManyToOne
  @JoinColumn(name = "gameId")
  public Replay getReplay() {
    return replay;
  }
}
