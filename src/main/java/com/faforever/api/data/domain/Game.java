package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.GameEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "game_stats")
@Include(rootLevel = true, type = "game")
@Immutable
@Setter
@EntityListeners(GameEnricher.class)
public class Game {

  private int id;
  private Instant startTime;
  private Instant endTime;
  private VictoryCondition victoryCondition;
  private FeaturedMod featuredMod;
  private Player host;
  private MapVersion mapVersion;
  private String name;
  private Validity validity;
  private List<GamePlayerStats> playerStats;
  private String replayUrl;
  private List<GameReview> reviews;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "startTime")
  public Instant getStartTime() {
    return startTime;
  }

  @Column(name = "gameType")
  public VictoryCondition getVictoryCondition() {
    return victoryCondition;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gameMod")
  public FeaturedMod getFeaturedMod() {
    return featuredMod;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "host")
  public Player getHost() {
    return host;
  }

  @ManyToOne(fetch = FetchType.LAZY)
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
  public Validity getValidity() {
    return validity;
  }

  @OneToMany(mappedBy = "game")
  public List<GamePlayerStats> getPlayerStats() {
    return playerStats;
  }

  @Formula(value = "(SELECT game_player_stats.scoreTime FROM game_player_stats WHERE game_player_stats.gameId = id ORDER BY game_player_stats.scoreTime DESC LIMIT 1)")
  @Nullable
  public Instant getEndTime() {
    return endTime;
  }

  @Transient
  @ComputedAttribute
  public String getReplayUrl() {
    return replayUrl;
  }

  @OneToMany(mappedBy = "game")
  public List<GameReview> getReviews() {
    return reviews;
  }
}
