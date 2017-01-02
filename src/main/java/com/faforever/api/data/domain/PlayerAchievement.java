package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "player_achievements")
@Include(rootLevel = true, type = "player_achievement")
public class PlayerAchievement {

  private int id;
  private Integer currentSteps;
  private AchievementState state;
  private Timestamp createTime;
  private Timestamp updateTime;
  private Player player;
  private AchievementDefinition achievement;

  @Id
  @Column(name = "id")
  @GeneratedValue
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Basic
  @Column(name = "current_steps")
  public Integer getCurrentSteps() {
    return currentSteps;
  }

  public void setCurrentSteps(Integer currentSteps) {
    this.currentSteps = currentSteps;
  }

  @Basic
  @Column(name = "state")
  @Enumerated(EnumType.STRING)
  public AchievementState getState() {
    return state;
  }

  public void setState(AchievementState state) {
    this.state = state;
  }

  @Basic
  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Timestamp createTime) {
    this.createTime = createTime;
  }

  @Basic
  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Timestamp updateTime) {
    this.updateTime = updateTime;
  }

  @ManyToOne
  @JoinColumn(name = "player_id")
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  @OneToOne
  @JoinColumn(name = "achievement_id")
  public AchievementDefinition getAchievement() {
    return achievement;
  }

  public void setAchievement(AchievementDefinition achievement) {
    this.achievement = achievement;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, currentSteps, state, createTime, updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlayerAchievement that = (PlayerAchievement) o;
    return id == that.id &&
        Objects.equals(currentSteps, that.currentSteps) &&
        Objects.equals(state, that.state) &&
        Objects.equals(createTime, that.createTime) &&
        Objects.equals(updateTime, that.updateTime);
  }
}
