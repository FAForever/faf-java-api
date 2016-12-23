package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "game_player_stats")
@Include(rootLevel = true, type = "game_player_stats")
public class GamePlayerStatsEntity {

  private long id;
  private PlayerEntity player;
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
  private ReplayEntity replay;

  @Id
  @Column(name = "id")
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @ManyToOne
  @JoinColumn(name = "playerId")
  public PlayerEntity getPlayer() {
    return player;
  }

  public void setPlayer(PlayerEntity player) {
    this.player = player;
  }

  @Basic
  @Column(name = "AI")
  public byte getAi() {
    return ai;
  }

  public void setAi(byte ai) {
    this.ai = ai;
  }

  @Basic
  @Column(name = "faction")
  public byte getFaction() {
    return faction;
  }

  public void setFaction(byte faction) {
    this.faction = faction;
  }

  @Basic
  @Column(name = "color")
  public byte getColor() {
    return color;
  }

  public void setColor(byte color) {
    this.color = color;
  }

  @Basic
  @Column(name = "team")
  public byte getTeam() {
    return team;
  }

  public void setTeam(byte team) {
    this.team = team;
  }

  @Basic
  @Column(name = "place")
  public byte getPlace() {
    return place;
  }

  public void setPlace(byte place) {
    this.place = place;
  }

  @Basic
  @Column(name = "mean")
  public Double getMean() {
    return mean;
  }

  public void setMean(Double mean) {
    this.mean = mean;
  }

  @Basic
  @Column(name = "deviation")
  public Double getDeviation() {
    return deviation;
  }

  public void setDeviation(Double deviation) {
    this.deviation = deviation;
  }

  @Basic
  @Column(name = "after_mean")
  public Double getAfterMean() {
    return afterMean;
  }

  public void setAfterMean(Double afterMean) {
    this.afterMean = afterMean;
  }

  @Basic
  @Column(name = "after_deviation")
  public Double getAfterDeviation() {
    return afterDeviation;
  }

  public void setAfterDeviation(Double afterDeviation) {
    this.afterDeviation = afterDeviation;
  }

  @Basic
  @Column(name = "score")
  public byte getScore() {
    return score;
  }

  public void setScore(byte score) {
    this.score = score;
  }

  @Basic
  @Column(name = "scoreTime")
  public Timestamp getScoreTime() {
    return scoreTime;
  }

  public void setScoreTime(Timestamp scoreTime) {
    this.scoreTime = scoreTime;
  }

  @ManyToOne
  @JoinColumn(name = "gameId")
  public ReplayEntity getReplay() {
    return replay;
  }

  public void setReplay(ReplayEntity replay) {
    this.replay = replay;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, replay, player, ai, faction, color, team, place, mean, deviation, afterMean, afterDeviation, score, scoreTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GamePlayerStatsEntity that = (GamePlayerStatsEntity) o;
    return id == that.id &&
        ai == that.ai &&
        faction == that.faction &&
        color == that.color &&
        team == that.team &&
        place == that.place &&
        score == that.score &&
        Objects.equals(replay, that.replay) &&
        Objects.equals(player, that.player) &&
        Objects.equals(mean, that.mean) &&
        Objects.equals(deviation, that.deviation) &&
        Objects.equals(afterMean, that.afterMean) &&
        Objects.equals(afterDeviation, that.afterDeviation) &&
        Objects.equals(scoreTime, that.scoreTime);
  }
}
