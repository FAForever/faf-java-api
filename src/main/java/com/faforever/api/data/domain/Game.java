package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "game_stats")
@Include(rootLevel = true, type = "game")
@Immutable
@Setter
public class Game {

  private int id;
  private Timestamp startTime;
  private VictoryCondition victoryCondition;
  private FeaturedMod featuredMod;
  private Player host;
  private MapVersion mapVersion;
  private String name;
  private Rankiness rankiness;
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

  @ManyToOne
  @JoinColumn(name = "gameMod")
  public FeaturedMod getFeaturedMod() {
    return featuredMod;
  }

  @ManyToOne
  @JoinColumn(name = "host")
  public Player getHost() {
    return host;
  }

  @ManyToOne
  @JoinColumn(name = "mapId")
  public MapVersion getMapVersion() {
    return mapVersion;
  }

  @Column(name = "gameName")
  public String getName() {
    return name;
  }

  @Column(name = "validity")
  @Enumerated(EnumType.ORDINAL)
  public Rankiness getRankiness() {
    return rankiness;
  }

  @OneToMany(mappedBy = "game")
  public List<GamePlayerStats> getPlayerStats() {
    return playerStats;
  }
}
