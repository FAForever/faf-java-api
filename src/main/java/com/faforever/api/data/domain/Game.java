package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.GameEnricher;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "game_stats")
@Include(rootLevel = true, type = "game")
@Immutable
@Getter
@Setter
@EntityListeners(GameEnricher.class)
public class Game {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "startTime")
  private OffsetDateTime startTime;

  @Column(name = "endTime")
  @Nullable
  private OffsetDateTime endTime;

  @Column(name = "gameType")
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
  private List<GamePlayerStats> playerStats;

  @Transient
  @ComputedAttribute
  private String replayUrl;

  @OneToMany(mappedBy = "game")
  @UpdatePermission(expression = "Prefab.Role.All")
  private List<GameReview> reviews;

  @OneToOne(fetch = FetchType.LAZY)
  @PrimaryKeyJoinColumn
  @UpdatePermission(expression = "Prefab.Role.All")
  @BatchSize(size = 1000)
  private GameReviewsSummary reviewsSummary;
}
