package com.faforever.api.league.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Setter
@Table(name = "league_season")
@Include(name = LeagueSeason.TYPE_NAME)
public class LeagueSeason {
  public static final String TYPE_NAME = "leagueSeason";

  private Integer id;
  private League league;
  private Leaderboard leaderboard;
  private Integer placementGames;
  private Integer placementGamesReturningPlayer;
  private Integer seasonNumber;
  private String nameKey;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "league_id")
  public League getLeague() {
    return league;
  }

  @ManyToOne
  @JoinColumn(name = "leaderboard_id")
  public Leaderboard getLeaderboard() {
    return leaderboard;
  }

  @Column(name = "placement_games")
  public Integer getPlacementGames() {
    return placementGames;
  }

  @Column(name = "placement_games_returning_player")
  public Integer getPlacementGamesReturningPlayer() {return placementGamesReturningPlayer;}

  @Column(name = "season_number")
  public Integer getSeasonNumber() {
    return seasonNumber;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "start_date")
  public OffsetDateTime getStartDate() {
    return startDate;
  }

  @Column(name = "end_date")
  public OffsetDateTime getEndDate() {
    return endDate;
  }
}
