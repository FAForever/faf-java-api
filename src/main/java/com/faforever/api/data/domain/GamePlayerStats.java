package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "game_player_stats")
@Include(rootLevel = true, type = "gamePlayerStats")
@Immutable
@Setter
public class GamePlayerStats {

  private long id;
  private Player player;
  private boolean ai;
  private Faction faction;
  private byte color;
  private byte team;
  private byte startSpot;
  private Double beforeMean;
  private Double beforeDeviation;
  private Double afterMean;
  private Double afterDeviation;
  private byte score;
  private OffsetDateTime scoreTime;
  private Game game;

  @Id
  @Column(name = "id")
  public long getId() {
    return id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "playerId")
  public Player getPlayer() {
    return player;
  }

  @Column(name = "AI")
  public boolean getAi() {
    return ai;
  }

  @Column(name = "faction")
  public Faction getFaction() {
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
  public byte getStartSpot() {
    return startSpot;
  }

  @Column(name = "mean")
  public Double getBeforeMean() {
    return beforeMean;
  }

  @Column(name = "deviation")
  public Double getBeforeDeviation() {
    return beforeDeviation;
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
  public OffsetDateTime getScoreTime() {
    return scoreTime;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gameId")
  public Game getGame() {
    return game;
  }
}
