package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "game_stats")
@Include(rootLevel = true, type = "replay")
@Immutable
@Setter
public class Replay {

  private int id;
  private Timestamp startTime;
  private VictoryCondition victoryCondition;
  private byte gameMod;
  private Player host;
  private MapVersion map;
  private String gameName;
  private byte validity;
  private List<GamePlayerStats> playerStats;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "startTime")
  public Timestamp getStartTime() {
    return startTime;
  }

  @Column(name = "gameType")
  public VictoryCondition getVictoryCondition() {
    return victoryCondition;
  }

  @Column(name = "gameMod")
  public byte getGameMod() {
    return gameMod;
  }

  @ManyToOne
  @JoinColumn(name = "host")
  public Player getHost() {
    return host;
  }

  @ManyToOne
  @JoinColumn(name = "mapId")
  public MapVersion getMap() {
    return map;
  }

  @Column(name = "gameName")
  public String getGameName() {
    return gameName;
  }

  @Column(name = "validity")
  public byte getValidity() {
    return validity;
  }

  @OneToMany(mappedBy = "replay")
  public List<GamePlayerStats> getPlayerStats() {
    return playerStats;
  }
}
