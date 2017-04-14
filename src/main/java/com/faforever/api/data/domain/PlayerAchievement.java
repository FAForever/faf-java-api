package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "player_achievements")
@Include(rootLevel = true, type = "playerAchievement")
@Setter
public class PlayerAchievement {

  private int id;
  private Integer currentSteps;
  private AchievementState state;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private Player player;
  private int playerId;
  private AchievementDefinition achievement;

  @Id
  @Column(name = "id")
  @GeneratedValue
  public int getId() {
    return id;
  }

  @Column(name = "current_steps")
  public Integer getCurrentSteps() {
    return currentSteps;
  }

  @Column(name = "state")
  @Enumerated(EnumType.STRING)
  public AchievementState getState() {
    return state;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @Exclude
  @Column(name = "player_id")
  public int getPlayerId() {
    return playerId;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id", insertable = false, updatable = false)
  public Player getPlayer() {
    return player;
  }

  @OneToOne
  @JoinColumn(name = "achievement_id")
  public AchievementDefinition getAchievement() {
    return achievement;
  }
}
