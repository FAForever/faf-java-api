package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

import static com.faforever.api.data.domain.PlayerAchievement.TYPE_NAME;

@Entity
@Table(name = "player_achievements")
@Include(name = TYPE_NAME)
@Data
@NoArgsConstructor
public class PlayerAchievement implements DefaultEntity {

  public static final String TYPE_NAME = "playerAchievement";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "current_steps")
  private Integer currentSteps;

  @Column(name = "state")
  @Enumerated(EnumType.STRING)
  private AchievementState state;

  @Column(name = "player_id")
  private int playerId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "player_id", insertable = false, updatable = false)
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "achievement_id")
  private Achievement achievement;
}
