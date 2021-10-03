package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "leaderboard_rating_journal")
@Include(name = LeaderboardRatingJournal.TYPE_NAME)
public class LeaderboardRatingJournal extends AbstractEntity<LeaderboardRatingJournal> {

  public static final String TYPE_NAME = "leaderboardRatingJournal";

  private Double meanBefore;
  private Double deviationBefore;
  private Double meanAfter;
  private Double deviationAfter;
  private Leaderboard leaderboard;
  private GamePlayerStats gamePlayerStats;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_player_stats_id")
  public GamePlayerStats getGamePlayerStats() {
    return gamePlayerStats;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leaderboard_id")
  public Leaderboard getLeaderboard() {
    return leaderboard;
  }

  @Column(name = "rating_mean_before")
  public Double getMeanBefore() {
    return meanBefore;
  }

  @Column(name = "rating_deviation_before")
  public Double getDeviationBefore() {
    return deviationBefore;
  }

  @Column(name = "rating_mean_after")
  public Double getMeanAfter() {
    return meanAfter;
  }

  @Column(name = "rating_deviation_after")
  public Double getDeviationAfter() {
    return deviationAfter;
  }
}
