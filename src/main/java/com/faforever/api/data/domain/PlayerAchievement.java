package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "player_achievements")
@Include(rootLevel = true, type = "playerAchievement")
@Getter
@Setter
public class PlayerAchievement extends AbstractEntity {

  @Column(name = "current_steps")
  private Integer currentSteps;

  @Column(name = "state")
  @Enumerated(EnumType.STRING)
  private AchievementState state;

  @Exclude
  @Column(name = "player_id")
  private int playerId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "player_id", insertable = false, updatable = false)
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "achievement_id")
  private Achievement achievement;
}
