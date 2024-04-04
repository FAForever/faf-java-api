package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.GameEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;
import org.jetbrains.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.Set;

import static com.faforever.api.data.domain.Game.TYPE_NAME;

@Entity
@Table(name = "game_stats")
@Include(name = TYPE_NAME)
@Immutable
@Setter
@ToString(onlyExplicitlyIncluded = true, doNotUseGetters = true)
@EntityListeners(GameEnricher.class)
public class Game {
  public final static String TYPE_NAME = "game";

  @ToString.Include(rank = 1)
  private int id;
  private OffsetDateTime startTime;
  private OffsetDateTime endTime;
  private Long replayTicks;
  private VictoryCondition victoryCondition;
  private FeaturedMod featuredMod;
  private Player host;
  private MapVersion mapVersion;
  @ToString.Include
  private String name;
  private Validity validity;
  private Set<GamePlayerStats> playerStats;
  private String replayUrl;
  private Set<GameReview> reviews;
  private GameReviewsSummary reviewsSummary;
  private Boolean replayAvailable;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "startTime")
  public OffsetDateTime getStartTime() {
    return startTime;
  }

  @Column(name = "replay_ticks")
  public Long getReplayTicks() {
    return replayTicks;
  }

  @Column(name = "gameType")
  @Enumerated(EnumType.STRING)
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
  @BatchSize(size = 1000)
  public Set<GamePlayerStats> getPlayerStats() {
    return playerStats;
  }

  @Column(name = "endTime")
  @Nullable
  public OffsetDateTime getEndTime() {
    return endTime;
  }

  @Transient
  @ComputedAttribute
  public String getReplayUrl() {
    return replayUrl;
  }

  @OneToMany(mappedBy = "game")
  @UpdatePermission(expression = Prefab.ALL)
  @BatchSize(size = 1000)
  public Set<GameReview> getReviews() {
    return reviews;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  @UpdatePermission(expression = Prefab.ALL)
  public GameReviewsSummary getReviewsSummary() {
    return reviewsSummary;
  }

  @Column(name = "replay_available")
  public Boolean isReplayAvailable() {
    return replayAvailable;
  }

  /**
   * This ManyToOne relationship leads to a double left outer join through Elide causing an additional full table
   * scan on the matchmaker_queue table. Even though it has only 3 records, it causes MySql 5.7 and MySQL to run
   * a list of all games > 1 min on prod where it was ~1 second before.
   *
   * This can be fixed by migrating to MariaDB.
   */
//  private Integer matchmakerQueueId;
//
//  @JoinTable(name = "matchmaker_queue_game",
//    joinColumns = @JoinColumn(name = "game_stats_id"),
//    inverseJoinColumns = @JoinColumn(name = "matchmaker_queue_id")
//  )
//  @ManyToOne(fetch = FetchType.LAZY)
//  @Nullable
//  public Integer getMatchmakerQueueId() {
//    return matchmakerQueueId;
//  }
}
