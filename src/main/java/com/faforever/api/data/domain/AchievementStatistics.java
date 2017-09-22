package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "achievement_statistics")
@Immutable
@Include(rootLevel = true, type = "achievementStatistics")
@Setter
public class AchievementStatistics {

  private String id;
  private Achievement achievement;
  private long unlockersCount;
  private BigDecimal unlockersPercent;
  private Long unlockersMinDuration;
  private Long unlockersAvgDuration;
  private Long unlockersMaxDuration;

  @Id
  @Column(name = "achievement_id")
  public String getId() {
    return id;
  }

  @OneToOne
  @JoinColumn(name = "achievement_id")
  public Achievement getAchievement() {
    return achievement;
  }

  @Column(name = "unlockers_count")
  public long getUnlockersCount() {
    return unlockersCount;
  }

  @Column(name = "unlockers_percent")
  public BigDecimal getUnlockersPercent() {
    return unlockersPercent;
  }

  @Column(name = "unlockers_min_duration")
  public Long getUnlockersMinDuration() {
    return unlockersMinDuration;
  }

  @Column(name = "unlockers_avg_duration")
  public Long getUnlockersAvgDuration() {
    return unlockersAvgDuration;
  }

  @Column(name = "unlockers_max_duration")
  public Long getUnlockersMaxDuration() {
    return unlockersMaxDuration;
  }
}
