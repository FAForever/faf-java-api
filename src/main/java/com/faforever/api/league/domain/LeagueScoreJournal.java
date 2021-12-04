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
@Table(name = "league_score_journal")
@Include(name = LeagueScoreJournal.TYPE_NAME)
public class LeagueScoreJournal {
  public static final String TYPE_NAME = "leagueScoreJournal";

  private Integer id;
  private Integer loginId;
  private LeagueSeason leagueSeason;
  private LeagueSeasonDivisionSubdivision leagueSeasonDivisionSubdivisionBefore;
  private LeagueSeasonDivisionSubdivision leagueSeasonDivisionSubdivisionAfter;
  private Integer scoreBefore;
  private Integer scoreAfter;
  private Integer gameCount;

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
  @JoinColumn(name = "subdivision_id_before")
  public LeagueSeasonDivisionSubdivision getLeagueSeasonDivisionSubdivisionBefore() {
    return leagueSeasonDivisionSubdivisionBefore;
  }

  @ManyToOne
  @JoinColumn(name = "subdivision_id_after")
  public LeagueSeasonDivisionSubdivision getLeagueSeasonDivisionSubdivisionAfter() {
    return leagueSeasonDivisionSubdivisionAfter;
  }

  @Column(name = "score_before")
  public Integer getScoreBefore() {
    return scoreBefore;
  }

  @Column(name = "score_after")
  public Integer getScoreAfter() {
    return scoreAfter;
  }

  @Column(name = "game_count")
  public Integer getGameCount() {
    return gameCount;
  }
}
