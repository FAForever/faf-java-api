package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.converter.VictoryConditionConverter;
import com.faforever.api.data.listeners.GameEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.Set;

import static com.faforever.api.data.domain.Game.TYPE_NAME;

@Entity
@Table(name = "game_stats")
@Include(name = TYPE_NAME)
@Immutable
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = false)
@EntityListeners(GameEnricher.class)
public class Game {

  public static final String TYPE_NAME = "game";

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  @ToString.Include
  private int id;

  @Column(name = "startTime")
  private OffsetDateTime startTime;

  @Column(name = "endTime")
  @Nullable
  private OffsetDateTime endTime;

  @Column(name = "replay_ticks")
  private Long replayTicks;

  @Column(name = "gameType")
  @Convert(converter = VictoryConditionConverter.class)
  private VictoryCondition victoryCondition;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gameMod")
  private FeaturedMod featuredMod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "host")
  private Player host;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mapId")
  private MapVersion mapVersion;

  @Column(name = "gameName")
  private String name;

  @Column(name = "validity")
  @Enumerated(EnumType.ORDINAL)
  private Validity validity;

  @OneToMany(mappedBy = "game")
  @BatchSize(size = 1000)
  private Set<GamePlayerStats> playerStats;

  @Transient
  @ComputedAttribute
  private String replayUrl;

  @OneToMany(mappedBy = "game")
  @UpdatePermission(expression = Prefab.ALL)
  @BatchSize(size = 1000)
  private Set<GameReview> reviews;

  @OneToOne(fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  @UpdatePermission(expression = Prefab.ALL)
  @BatchSize(size = 1000)
  private GameReviewsSummary reviewsSummary;

  @Column(name = "replay_available")
  private Boolean replayAvailable;

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
