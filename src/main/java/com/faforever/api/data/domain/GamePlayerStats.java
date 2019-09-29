package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Getter;
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
@Getter
@Setter
public class GamePlayerStats {

  @Id
  @Column(name = "id")
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "playerId")
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

  @Column(name = "mean")
  private Double beforeMean;

  @Column(name = "deviation")
  private Double beforeDeviation;

  @Column(name = "after_mean")
  private Double afterMean;

  @Column(name = "after_deviation")
  private Double afterDeviation;

  @Column(name = "score")
  private Byte score;

  @Column(name = "scoreTime")
  private OffsetDateTime scoreTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gameId")
  private Game game;
}
