package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "game_stats")
@Include(rootLevel = true, type = "replay")
public class ReplayEntity {

  private int id;
  private Timestamp startTime;
  private VictoryCondition victoryCondition;
  private byte gameMod;
  private PlayerEntity host;
  private MapVersionEntity map;
  private String gameName;
  private byte validity;
  private List<GamePlayerStatsEntity> playerStats;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Basic
  @Column(name = "startTime")
  public Timestamp getStartTime() {
    return startTime;
  }

  public void setStartTime(Timestamp startTime) {
    this.startTime = startTime;
  }

  @Basic
  @Column(name = "gameType")
  public VictoryCondition getVictoryCondition() {
    return victoryCondition;
  }

  public void setVictoryCondition(VictoryCondition gameType) {
    this.victoryCondition = gameType;
  }

  @Basic
  @Column(name = "gameMod")
  public byte getGameMod() {
    return gameMod;
  }

  public void setGameMod(byte gameMod) {
    this.gameMod = gameMod;
  }

  @ManyToOne
  @JoinColumn(name = "host")
  public PlayerEntity getHost() {
    return host;
  }

  public void setHost(PlayerEntity host) {
    this.host = host;
  }

  @ManyToOne
  @JoinColumn(name = "mapId")
  public MapVersionEntity getMap() {
    return map;
  }

  public void setMap(MapVersionEntity map) {
    this.map = map;
  }

  @Basic
  @Column(name = "gameName")
  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  @Basic
  @Column(name = "validity")
  public byte getValidity() {
    return validity;
  }

  public void setValidity(byte validity) {
    this.validity = validity;
  }

  @OneToMany(mappedBy = "replay")
  public List<GamePlayerStatsEntity> getPlayerStats() {
    return playerStats;
  }

  public void setPlayerStats(List<GamePlayerStatsEntity> playerStats) {
    this.playerStats = playerStats;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, startTime, victoryCondition, gameMod, host, map, gameName, validity);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReplayEntity that = (ReplayEntity) o;
    return id == that.id &&
        gameMod == that.gameMod &&
        validity == that.validity &&
        Objects.equals(startTime, that.startTime) &&
        Objects.equals(victoryCondition, that.victoryCondition) &&
        Objects.equals(host, that.host) &&
        Objects.equals(map, that.map) &&
        Objects.equals(gameName, that.gameName);
  }
}
