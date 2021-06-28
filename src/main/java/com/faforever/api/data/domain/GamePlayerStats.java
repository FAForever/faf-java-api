package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Set;

import static com.faforever.api.data.domain.GamePlayerStats.TYPE_NAME;

@Entity
@Table(name = "game_player_stats")
@Include(name = TYPE_NAME)
@Immutable
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class GamePlayerStats {

  public static final String TYPE_NAME = "gamePlayerStats";

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  @ToString.Include
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "playerId")
  @ToString.Include
  private Player player;

  @Column(name = "AI")
  private boolean ai;

  @Column(name = "faction")
  private Faction faction;

  @Column(name = "color")
  private byte color;

  @Column(name = "team")
  private byte team;

  @Column(name = "place")
  private byte startSpot;

  @Deprecated
  @Column(name = "mean")
  private Double beforeMean;

  @Deprecated
  @Column(name = "deviation")
  private Double beforeDeviation;

  @Deprecated
  @Column(name = "after_mean")
  private Double afterMean;

  @Deprecated
  @Column(name = "after_deviation")
  private Double afterDeviation;

  @Column(name = "score")
  private Byte score;

  @Column(name = "scoreTime")
  private OffsetDateTime scoreTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gameId")
  private Game game;

  @Column(name = "result")
  @Enumerated(EnumType.STRING)
  private GameOutcome result;

  @OneToMany(mappedBy = "gamePlayerStats")
  @BatchSize(size = 1000)
  private Set<LeaderboardRatingJournal> ratingChanges;
}
