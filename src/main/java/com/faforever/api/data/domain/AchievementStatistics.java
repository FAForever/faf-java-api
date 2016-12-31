package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import org.hibernate.annotations.Immutable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "achievement_statistics")
@Immutable
@Include(rootLevel = true, type = "achievement_statistics")
public class AchievementStatistics {

  private String id;
  private AchievementDefinition achievement;
  private long unlockersCount;
  private BigDecimal unlockersPercent;
  private Long unlockersMinDuration;
  private Integer unlockersAvgDuration;
  private Long unlockersMaxDuration;

  @Id
  @Column(name = "achievement_id")
  public String getId() {
    return id;
  }

  public void setId(String achievementId) {
    this.id = achievementId;
  }

  @OneToOne
  @JoinColumn(name = "achievement_id")
  public AchievementDefinition getAchievement() {
    return achievement;
  }

  public void setAchievement(AchievementDefinition achievement) {
    this.achievement = achievement;
  }

  @Basic
  @Column(name = "unlockers_count")
  public long getUnlockersCount() {
    return unlockersCount;
  }

  public void setUnlockersCount(long unlockersCount) {
    this.unlockersCount = unlockersCount;
  }

  @Basic
  @Column(name = "unlockers_percent")
  public BigDecimal getUnlockersPercent() {
    return unlockersPercent;
  }

  public void setUnlockersPercent(BigDecimal unlockersPercent) {
    this.unlockersPercent = unlockersPercent;
  }

  @Basic
  @Column(name = "unlockers_min_duration")
  public Long getUnlockersMinDuration() {
    return unlockersMinDuration;
  }

  public void setUnlockersMinDuration(Long unlockersMinDuration) {
    this.unlockersMinDuration = unlockersMinDuration;
  }

  @Basic
  @Column(name = "unlockers_avg_duration")
  public Integer getUnlockersAvgDuration() {
    return unlockersAvgDuration;
  }

  public void setUnlockersAvgDuration(Integer unlockersAvgDuration) {
    this.unlockersAvgDuration = unlockersAvgDuration;
  }

  @Basic
  @Column(name = "unlockers_max_duration")
  public Long getUnlockersMaxDuration() {
    return unlockersMaxDuration;
  }

  public void setUnlockersMaxDuration(Long unlockersMaxDuration) {
    this.unlockersMaxDuration = unlockersMaxDuration;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, unlockersCount, unlockersPercent, unlockersMinDuration, unlockersAvgDuration, unlockersMaxDuration);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AchievementStatistics that = (AchievementStatistics) o;
    return unlockersCount == that.unlockersCount &&
        Objects.equals(id, that.id) &&
        Objects.equals(unlockersPercent, that.unlockersPercent) &&
        Objects.equals(unlockersMinDuration, that.unlockersMinDuration) &&
        Objects.equals(unlockersAvgDuration, that.unlockersAvgDuration) &&
        Objects.equals(unlockersMaxDuration, that.unlockersMaxDuration);
  }
}
