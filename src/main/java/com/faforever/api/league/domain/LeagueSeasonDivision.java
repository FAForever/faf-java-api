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
@Table(name = "league_season_division")
@Include(name = LeagueSeasonDivision.TYPE_NAME)
public class LeagueSeasonDivision {
  public static final String TYPE_NAME = "leagueSeasonDivision";

  private Integer id;
  private LeagueSeason leagueSeason;
  private Integer divisionIndex;
  private String nameKey;
  private String descriptionKey;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "league_season_id")
  public LeagueSeason getLeagueSeason() {
    return leagueSeason;
  }

  @Column(name = "division_index")
  public Integer getDivisionIndex() {
    return divisionIndex;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }
}
