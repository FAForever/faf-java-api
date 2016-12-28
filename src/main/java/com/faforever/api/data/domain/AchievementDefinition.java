package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "achievement_definitions")
@Include(rootLevel = true, type = "achievement_definition")
public class AchievementDefinition {

  private String id;
  private int order;
  private String nameKey;
  private String descriptionKey;
  private AchievementType type;
  private Integer totalSteps;
  private String revealedIconUrl;
  private String unlockedIconUrl;
  private AchievementState initialState;
  private int experiencePoints;
  private Timestamp createTime;
  private Timestamp updateTime;

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Basic
  @Column(name = "order")
  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @Basic
  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  public void setNameKey(String nameKey) {
    this.nameKey = nameKey;
  }

  @Basic
  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  public void setDescriptionKey(String descriptionKey) {
    this.descriptionKey = descriptionKey;
  }

  @Basic
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public AchievementType getType() {
    return type;
  }

  public void setType(AchievementType type) {
    this.type = type;
  }

  @Basic
  @Column(name = "total_steps")
  public Integer getTotalSteps() {
    return totalSteps;
  }

  public void setTotalSteps(Integer totalSteps) {
    this.totalSteps = totalSteps;
  }

  @Basic
  @Column(name = "revealed_icon_url")
  public String getRevealedIconUrl() {
    return revealedIconUrl;
  }

  public void setRevealedIconUrl(String revealedIconUrl) {
    this.revealedIconUrl = revealedIconUrl;
  }

  @Basic
  @Column(name = "unlocked_icon_url")
  public String getUnlockedIconUrl() {
    return unlockedIconUrl;
  }

  public void setUnlockedIconUrl(String unlockedIconUrl) {
    this.unlockedIconUrl = unlockedIconUrl;
  }

  @Basic
  @Column(name = "initial_state")
  @Enumerated(value = EnumType.STRING)
  public AchievementState getInitialState() {
    return initialState;
  }

  public void setInitialState(AchievementState initialState) {
    this.initialState = initialState;
  }

  @Basic
  @Column(name = "experience_points")
  public int getExperiencePoints() {
    return experiencePoints;
  }

  public void setExperiencePoints(int experiencePoints) {
    this.experiencePoints = experiencePoints;
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

  @Override
  public int hashCode() {
    return Objects.hash(id, order, nameKey, descriptionKey, type, totalSteps, revealedIconUrl, unlockedIconUrl, initialState, experiencePoints, createTime, updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AchievementDefinition that = (AchievementDefinition) o;
    return order == that.order &&
        experiencePoints == that.experiencePoints &&
        Objects.equals(id, that.id) &&
        Objects.equals(nameKey, that.nameKey) &&
        Objects.equals(descriptionKey, that.descriptionKey) &&
        Objects.equals(type, that.type) &&
        Objects.equals(totalSteps, that.totalSteps) &&
        Objects.equals(revealedIconUrl, that.revealedIconUrl) &&
        Objects.equals(unlockedIconUrl, that.unlockedIconUrl) &&
        Objects.equals(initialState, that.initialState) &&
        Objects.equals(createTime, that.createTime) &&
        Objects.equals(updateTime, that.updateTime);
  }
}
