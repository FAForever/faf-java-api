package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

/**
 * A player whose client reported a desync in a game via GPGNet. Written by the lobby server only; one row per game and
 * player, recording the first report.
 */
@Entity
@Table(name = "game_desync")
@Include(name = "gameDesync")
@Immutable
@Setter
@CreatePermission(expression = Prefab.NONE)
@UpdatePermission(expression = Prefab.NONE)
@DeletePermission(expression = Prefab.NONE)
public class GameDesync {

  private int id;
  private Game game;
  private Player player;
  private OffsetDateTime reportedAt;
  private Integer gameTime;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public int getId() {
    return id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id")
  public Game getGame() {
    return game;
  }

  /**
   * The player whose client reported the desync. Every client that detects the mismatch reports it, so this is not the
   * culprit.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  public Player getPlayer() {
    return player;
  }

  @Column(name = "reported_at")
  public OffsetDateTime getReportedAt() {
    return reportedAt;
  }

  /**
   * Seconds since the game was launched, derived by the lobby server. Not a simulation tick.
   */
  @Column(name = "game_time")
  public Integer getGameTime() {
    return gameTime;
  }
}
