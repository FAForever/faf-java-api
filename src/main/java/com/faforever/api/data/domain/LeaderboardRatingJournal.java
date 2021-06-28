package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "leaderboard_rating_journal")
@Include(name = LeaderboardRatingJournal.TYPE_NAME)
public class LeaderboardRatingJournal implements DefaultEntity {

  public static final String TYPE_NAME = "leaderboardRatingJournal";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "rating_mean_before")
  private Double meanBefore;

  @Column(name = "rating_deviation_before")
  private Double deviationBefore;

  @Column(name = "rating_mean_after")
  private Double meanAfter;

  @Column(name = "rating_deviation_after")
  private Double deviationAfter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leaderboard_id")
  @EqualsAndHashCode.Exclude
  private Leaderboard leaderboard;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_player_stats_id")
  @EqualsAndHashCode.Exclude
  private GamePlayerStats gamePlayerStats;
}
