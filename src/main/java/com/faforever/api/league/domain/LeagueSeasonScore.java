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

@Entity
@Setter
@Table(name = "league_season_score")
@Include(name = LeagueSeasonScore.TYPE_NAME)
public class LeagueSeasonScore {
  public static final String TYPE_NAME = "leagueSeasonScore";

  private Integer id;
  private Integer loginId;
  private LeagueSeason leagueSeason;
  private LeagueSeasonDivisionSubdivision leagueSeasonDivisionSubdivision;
  private Integer score;
  private Integer gameCount;
  private Boolean returningPlayer;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @Column(name = "login_id")
  public Integer getLoginId() {
    return loginId;
  }

  @ManyToOne
  @JoinColumn(name = "league_season_id")
  public LeagueSeason getLeagueSeason() {
    return leagueSeason;
  }

  @ManyToOne
  @JoinColumn(name = "subdivision_id")
  public LeagueSeasonDivisionSubdivision getLeagueSeasonDivisionSubdivision() {
    return leagueSeasonDivisionSubdivision;
  }

  @Column(name = "score")
  public Integer getScore() {
    return score;
  }

  @Column(name = "game_count")
  public Integer getGameCount() {
    return gameCount;
  }

  @Column(name = "returning_player")
  public Boolean getReturningPlayer() {
    return returningPlayer;
  }
}
