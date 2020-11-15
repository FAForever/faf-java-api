package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Setter
@Table(name = "leaderboard_rating")
@Include(rootLevel = true, type = LeaderboardRating.TYPE_NAME)
public class LeaderboardRating extends AbstractEntity implements OwnableEntity {

  public static final String TYPE_NAME = "leaderboardRating";

  private Double mean;
  private Double deviation;
  private double rating;
  private int totalGames;
  private int wonGames;
  private Leaderboard leaderboard;
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "login_id")
  public Player getPlayer() {
    return player;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leaderboard_id")
  public Leaderboard getLeaderboard() {
    return leaderboard;
  }

  @Column(name = "total_games")
  public int getTotalGames() {
    return totalGames;
  }

  @Column(name = "won_games")
  public int getWonGames() {
    return wonGames;
  }

  @Column(name = "mean")
  public Double getMean() {
    return mean;
  }

  @Column(name = "deviation")
  public Double getDeviation() {
    return deviation;
  }

  @Column(name = "rating")
  public double getRating() {
    return rating;
  }

  @Override
  @Transient
  public Login getEntityOwner() {
    return getPlayer();
  }
}
