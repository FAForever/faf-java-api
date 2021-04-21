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
@Table(name = "league_season_division_subdivision")
@Include(type = LeagueSeasonDivisionSubdivision.TYPE_NAME)
public class LeagueSeasonDivisionSubdivision {
  public static final String TYPE_NAME = "leagueSeasonDivisionSubdivision";

  private Integer id;
  private LeagueSeasonDivision leagueSeasonDivision;
  private Integer subdivisionIndex;
  private String nameKey;
  private String descriptionKey;
  private Double minRating;
  private Double maxRating;
  private Integer highestScore;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "league_season_division_id")
  public LeagueSeasonDivision getLeagueSeasonDivision() {
    return leagueSeasonDivision;
  }

  @Column(name = "subdivision_index")
  public Integer getSubdivisionIndex() {
    return subdivisionIndex;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  @Column(name = "min_rating")
  public Double getMinRating() {
    return minRating;
  }

  @Column(name = "max_rating")
  public Double getMaxRating() {
    return maxRating;
  }

  @Column(name = "highest_score")
  public Integer getHighestScore() {
    return highestScore;
  }
}
