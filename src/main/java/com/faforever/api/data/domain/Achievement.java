package com.faforever.api.data.domain;

import com.faforever.api.data.listeners.AchievementLocalizationListener;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "achievement_definitions")
@SecondaryTable(name = "achievement_statistics", pkJoinColumns = @PrimaryKeyJoinColumn(name = "achievement_id", referencedColumnName = "id"))
@Include(rootLevel = true, type = Achievement.TYPE_NAME)
@EntityListeners(AchievementLocalizationListener.class)
@Getter
@Setter
public class Achievement {

  public static final String TYPE_NAME = "achievement";

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "\"order\"")
  private int order;

  @Column(name = "name_key")
  @Exclude
  private String nameKey;

  @Column(name = "description_key")
  @Exclude
  private String descriptionKey;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private AchievementType type;

  @Column(name = "total_steps")
  private Integer totalSteps;

  @Column(name = "revealed_icon_url")
  private String revealedIconUrl;

  @Column(name = "unlocked_icon_url")
  private String unlockedIconUrl;

  @Column(name = "initial_state")
  @Enumerated(value = EnumType.STRING)
  private AchievementState initialState;

  @Column(name = "experience_points")
  private int experiencePoints;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "unlockers_count", table = "achievement_statistics")
  private long unlockersCount;

  @Column(name = "unlockers_percent", table = "achievement_statistics")
  private BigDecimal unlockersPercent;

  @Column(name = "unlockers_min_duration", table = "achievement_statistics")
  private Long unlockersMinDuration;

  @Column(name = "unlockers_avg_duration", table = "achievement_statistics")
  private Long unlockersAvgDuration;

  @Column(name = "unlockers_max_duration", table = "achievement_statistics")
  private Long unlockersMaxDuration;

  // Set by AchievementLocalizationListener
  @Transient
  @ComputedAttribute
  private String name;

  @Transient
  @ComputedAttribute
  private String description;
}
