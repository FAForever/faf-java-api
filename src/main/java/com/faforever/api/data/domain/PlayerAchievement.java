package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_achievements")
@Include(name = "playerAchievement")
@Setter
public class PlayerAchievement extends AbstractEntity<PlayerAchievement> {

  private Integer currentSteps;
  private AchievementState state;
  private Player player;
  private int playerId;
  private Achievement achievement;

  @Column(name = "current_steps")
  public Integer getCurrentSteps() {
    return currentSteps;
  }

  @Column(name = "state")
  @Enumerated(EnumType.STRING)
  public AchievementState getState() {
    return state;
  }

  @Exclude
  @Column(name = "player_id")
  public int getPlayerId() {
    return playerId;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "player_id", insertable = false, updatable = false)
  public Player getPlayer() {
    return player;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "achievement_id")
  public Achievement getAchievement() {
    return achievement;
  }
}
